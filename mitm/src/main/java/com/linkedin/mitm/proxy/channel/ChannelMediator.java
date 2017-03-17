/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.channel;

import com.linkedin.mitm.proxy.dataflow.ProxyModeController;
import com.linkedin.mitm.proxy.dataflow.ProxyModeControllerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLEngine;
import org.apache.log4j.Logger;


/**
 * Abstract connection flow and data flow between two channels: client channel and server channel
 * It will be created lazily based on definition of connection flow steps. Every new connection from
 * client side will create one instance of ChannelMediator and it has reference to both client channel
 * and server channel.
 *
 * @author shfeng
 */
public class ChannelMediator {
  private static final String MODULE = ChannelMediator.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private final ProxyModeControllerFactory _proxyModeControllerFactory;
  private ProxyModeController _proxyModeController;
  private final NioEventLoopGroup _upstreamWorkerGroup;
  private final int _serverConnectionIdleTimeoutMsec;
  private final Channel _clientChannel;
  private final ChannelGroup _allChannelGroup;
  private Channel _serverChannel;

  public ChannelMediator(Channel clientChannel, final ProxyModeControllerFactory proxyModeControllerFactory,
      final NioEventLoopGroup upstreamWorkerGroup, final int timeout, final ChannelGroup channelGroup) {
    _clientChannel = clientChannel;
    _proxyModeControllerFactory = proxyModeControllerFactory;
    _upstreamWorkerGroup = upstreamWorkerGroup;
    _serverConnectionIdleTimeoutMsec = timeout;
    _allChannelGroup = channelGroup;
  }

  public void initializeProxyModeController(HttpRequest initialRequest) {
    _proxyModeController = _proxyModeControllerFactory.create(initialRequest);
  }

  /**
   * register open channels in global channel group for bulk operations such as shutdown.
   * */
  public void registerChannel(Channel channel) {
    _allChannelGroup.add(channel);
  }

  /**
   * Write data to server
   *
   * */
  public ChannelFuture writeToServer(HttpObject httpObject) {
    HttpObject result = _proxyModeController.handleWriteToServer(httpObject);
    return writeToChannel(_serverChannel, result);
  }

  /**
   * Write data to client
   *
   * */
  public ChannelFuture writeToClient(HttpObject httpObject) {
    HttpObject result = _proxyModeController.handleWriteToClient(httpObject);
    return writeToChannel(_clientChannel, result);
  }

  /**
   * Write last content back to client and close channel.
   * */
  public void writeToClientAndDisconnect(HttpObject httpObject) {
    _proxyModeController.handleWriteToClient(httpObject);
    writeToChannel(_clientChannel, httpObject).addListener(future -> {
      if (future.isSuccess()) {
        disconnectBothChannels();
      } else {
        throw new IllegalStateException("Failed to write to client channel");
      }
    });
  }

  /**
   * Disconnect both client to proxy and proxy to server channels
   * */
  public void disconnectBothChannels() {
    disconnect(_clientChannel).addListener(clientFuture -> {
      if (!clientFuture.isSuccess()) {
        LOG.error(String.format("Failed to close client channel %s because: %s", _clientChannel, clientFuture.cause()));
      }
      if (_serverChannel != null) {
        disconnect(_serverChannel).addListener(serverFuture -> {
          if (!serverFuture.isSuccess()) {
            LOG.error(
                String.format("Failed to close server channel %s because: %s", _serverChannel, serverFuture.cause()));
          }
        });
      }
    });
  }

  /**
   * Establishing TCP connection to server
   *
   * @param remoteAddress remote address
   * */
  public ChannelFuture connectToServer(final InetSocketAddress remoteAddress) {
    if (remoteAddress == null) {
      throw new IllegalStateException("remote address is null");
    }
    Bootstrap bootstrap = new Bootstrap().group(_upstreamWorkerGroup);
    bootstrap.channelFactory(NioSocketChannel::new);
    ServerChannelHandler serverChannelHandler = new ServerChannelHandler(this);

    bootstrap.handler(new ChannelInitializer<Channel>() {
      protected void initChannel(Channel ch)
          throws Exception {
        initChannelPipeline(ch.pipeline(), serverChannelHandler, _serverConnectionIdleTimeoutMsec);
        _serverChannel = ch;
      }
    });
    LOG.debug("Server channel is ready. About to connect....");
    return bootstrap.connect(remoteAddress);
  }

  /**
   * Read data from client channel
   * */
  public void readFromClientChannel(HttpObject httpObject) {
    _proxyModeController.handleReadFromClient(this, httpObject);
  }

  /**
   * Read data from server channel
   * */
  public void readFromServerChannel(HttpObject httpObject) {
    _proxyModeController.handleReadFromServer(httpObject);
  }

  /**
   * Init handshaking to server
   *
   * @param sslEngine provided by JDK with complicated ssl handshaking logic
   * */
  public Future<Channel> handshakeWithServer(SSLEngine sslEngine) {
    return handshake(sslEngine, true, _serverChannel);
  }

  /**
   * Accept handshaking from client and complete it.
   *
   * @param sslEngine provided by JDK with complicated ssl handshaking logic
   * */
  public Future<Channel> handshakeWithClient(SSLEngine sslEngine) {
    return handshake(sslEngine, false, _clientChannel);
  }

  public ChannelFuture resumeReadingFromClientChannel() {
    if (_clientChannel == null) {
      throw new IllegalStateException("Channel can't be null");
    }
    _clientChannel.config().setAutoRead(true);
    return _clientChannel.newSucceededFuture();
  }

  public ChannelFuture stopReadingFromClientChannel() {
    if (_clientChannel == null) {
      throw new IllegalStateException("Channel can't be null");
    }
    _clientChannel.config().setAutoRead(false);
    return _clientChannel.newSucceededFuture();
  }

  /**
   * Create {@link io.netty.handler.ssl.SslHandler} and send TCP handshaking using
   * {@link javax.net.ssl.SSLEngine}
   * After add ssl handler to the end of {@link io.netty.channel.ChannelPipeline}, it enable
   * secure communications over SSL/TLS
   *
   * @param isSslClient true if the channel start handshaking or false if accept handshaking
   * @param channel the channel to start handshaking
   * */
  private Future<Channel> handshake(SSLEngine sslEngine, boolean isSslClient, Channel channel) {
    sslEngine.setUseClientMode(isSslClient);
    if (channel != null) {
      channel.config().setAutoRead(true);
    }
    SslHandler handler = new SslHandler(sslEngine);
    channel.pipeline().addFirst("ssl", handler);
    LOG.debug("About to start handshaking...");
    return handler.handshakeFuture();
  }

  private void initChannelPipeline(ChannelPipeline pipeline, ServerChannelHandler serverChannelHandler,
      int idleTimeoutMsec) {
    pipeline.addLast("decoder", new HttpResponseDecoder());
    pipeline.addLast("encoder", new HttpRequestEncoder());
    pipeline.addLast("idle", new IdleStateHandler(0, 0, idleTimeoutMsec / 1000));
    pipeline.addLast("handler", serverChannelHandler);
  }

  /**
   * This is the method that executing writing to channel.
   * It will be used both write0 and {@link com.linkedin.mitm.proxy.connectionflow.steps.ConnectionFlowStep}
   *
   * @param channel which channel to write to
   * @param object  which object to write to.
   *
   * */
  private ChannelFuture writeToChannel(final Channel channel, final Object object) {
    if (channel == null) {
      throw new IllegalStateException("Failed to write to channel because channel is null");
    }
    if (object instanceof ReferenceCounted) {
      LOG.debug("Retaining reference counted message");
      ((ReferenceCounted) object).retain();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("Writing in channel [%s]:  %s", channel.toString(), object));
    }
    return channel.writeAndFlush(object);
  }

  private Future<Void> disconnect(final Channel channel) {
    if (channel == null) {
      return null;
    }
    final Promise<Void> promise = channel.newPromise();
    writeToChannel(channel, Unpooled.EMPTY_BUFFER).addListener(future -> closeChannel(promise, channel));
    return promise;
  }

  private void closeChannel(final Promise<Void> promise, final Channel channel) {
    channel.close().addListener(future -> {
      if (future.isSuccess()) {
        promise.setSuccess(null);
      } else {
        promise.setFailure(future.cause());
      }
    });
  }
}
