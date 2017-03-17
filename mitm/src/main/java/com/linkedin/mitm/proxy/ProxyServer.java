/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy;

import com.linkedin.mitm.model.Protocol;
import com.linkedin.mitm.proxy.connectionflow.steps.ConnectionFlowStep;
import com.linkedin.mitm.proxy.dataflow.ProxyModeControllerFactory;
import com.linkedin.mitm.proxy.factory.NamedThreadFactory;
import io.netty.bootstrap.ChannelFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ServerChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 * Represent proxy server that holds a bunch of property that would be
 * used to start proxy server. It's singleton and all of properties will
 * be used by all channels.
 *
 * <pre>
 *   {@code
 *    ProxyServer proxyServer = new ProxyServer.Builder()
 *         .proxyModeControllerFactory(new NormalProxyModeControllerFactory())
 *         .connectionFlow(Protocol.HTTP, ConnectionFlowFactory.createFullHttpsConnectionFlow())
 *         .connectionFlow(Protocol.HTTPS, ConnectionFlowFactory.createFullHttpConnectionFlow())
 *         .build();
 *    proxyServer.start();
 *    //client code
 *    proxyServer.stop();
 * }
 * </pre>
 *
 * @author shfeng
 */
public class ProxyServer {
  private static final String MODULE = ProxyServer.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private final String _host;
  private final int _port;
  private final Map<Protocol, List<ConnectionFlowStep>> _connectionFlowRegistry;
  private final ProxyModeControllerFactory _proxyModeControllerFactory;
  private final NioEventLoopGroup _acceptorGroup;                   //acceptor thread pool
  private final NioEventLoopGroup _upstreamWorkerGroup;             //upstream worker thread pool
  private final NioEventLoopGroup _downstreamWorkerGroup;           //downstream worker thread pool
  private final int _serverConnectionIdleTimeout;
  private final int _clientConnectionIdleTimeout;
  private final ChannelGroup _allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

  /**
   * Start proxy server
   * */
  public void start()
      throws InterruptedException {
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap.group(_acceptorGroup, _upstreamWorkerGroup);
    serverBootstrap.channelFactory(new ChannelFactory<ServerChannel>() {
      @Override
      public ServerChannel newChannel() {
        return new NioServerSocketChannel();
      }
    });
    serverBootstrap.childHandler(new ProxyInitializer(this));

    //bind
    ChannelFuture future = serverBootstrap.bind(_host, _port);

    //wait for the future
    future.awaitUninterruptibly();
    if (!future.isSuccess()) {
      future.channel().closeFuture().awaitUninterruptibly();
      throw new ChannelException(String.format("Failed to bind to: %s:%d", _host, _port), future.cause());
    } else {
      _allChannels.add(future.channel());
    }
  }

  /**
   * Stop proxy server
   * */
  public void stop() {
    ChannelGroupFuture future = _allChannels.close().awaitUninterruptibly();
    if (!future.isSuccess()) {
      final Iterator<ChannelFuture> iter = future.iterator();
      while (iter.hasNext()) {
        final ChannelFuture cf = iter.next();
        if (!cf.isSuccess()) {
          LOG.warn(String.format("Failed to close channel %s because %s", cf.channel(), cf.cause()));
        }
      }
    }
    _acceptorGroup.shutdownGracefully();
    _upstreamWorkerGroup.shutdownGracefully();
    _downstreamWorkerGroup.shutdownGracefully();
  }

  /**
   * This property get passed in {@link com.linkedin.mitm.proxy.channel.ChannelMediator}
   * and keep track of all of open channels. If registered channel get closed correctly,
   * it will automatically get removed from this Channel Group
   * */
  ChannelGroup getAllChannels() {
    return _allChannels;
  }


  Map<Protocol, List<ConnectionFlowStep>> getConnectionFlowRegistry() {
    return _connectionFlowRegistry;
  }

  ProxyModeControllerFactory getProxyModeControllerFactory() {
    return _proxyModeControllerFactory;
  }

  NioEventLoopGroup getDownstreamWorkerGroup() {
    return _downstreamWorkerGroup;
  }

  int getServerConnectionIdleTimeout() {
    return _serverConnectionIdleTimeout;
  }

  int getClientConnectionIdleTimeout() {
    return _clientConnectionIdleTimeout;
  }

  private ProxyServer(final Builder builder) {
    _acceptorGroup =
        new NioEventLoopGroup(2, new NamedThreadFactory("Client acceptor group"), SelectorProvider.provider());
    _upstreamWorkerGroup =
        new NioEventLoopGroup(8, new NamedThreadFactory("Client worker group"), SelectorProvider.provider());
    _upstreamWorkerGroup.setIoRatio(80);
    _downstreamWorkerGroup =
        new NioEventLoopGroup(8, new NamedThreadFactory("Server worker group"), SelectorProvider.provider());
    _downstreamWorkerGroup.setIoRatio(80);
    _host = builder._host;
    _port = builder._port;
    _serverConnectionIdleTimeout = builder._serverChannelIdleTimeout;
    _clientConnectionIdleTimeout = builder._clientChannelIdleTimeout;
    _connectionFlowRegistry = builder._connectionFlowRegistry;
    _proxyModeControllerFactory = builder._proxyModeControllerFactory;
  }

  public static class Builder {
    private String _host = "127.0.0.1";
    private int _port = 5555;
    private int _clientChannelIdleTimeout = 40000;
    private int _serverChannelIdleTimeout = 4000;
    private ProxyModeControllerFactory _proxyModeControllerFactory;
    private final Map<Protocol, List<ConnectionFlowStep>> _connectionFlowRegistry = new HashMap<>();

    /**
     * @param proxyModeControllerFactory Factory of ProxyModeController which is used for generating the data flow which controls real data flow for proxy.
     *                                   Default implementation is {@link com.linkedin.mitm.proxy.dataflow.NormalProxyModeControllerFactory},
     *                                   which is used for generating regular http proxy data flow
     *
     * */
    public Builder proxyModeControllerFactory(ProxyModeControllerFactory proxyModeControllerFactory) {
      _proxyModeControllerFactory = proxyModeControllerFactory;
      return this;
    }

    /**
     * @param port proxy port number
     *        Default: 5555
     * */
    public Builder port(int port) {
      _port = port;
      return this;
    }

    /**
     * @param host proxy host address
     *        Default: localhost
     * */
    public Builder host(String host) {
      _host = host;
      return this;
    }

    /**
     * @param timeout server channel idle timeout
     *        Default: 4000
     * */
    public Builder serverIdleTimeout(int timeout) {
      _serverChannelIdleTimeout = timeout;
      return this;
    }

    /**
     * @param timeout client channel idle timeout
     *        Default: 40000
     * */
    public Builder clientIdleTimeout(int timeout) {
      _clientChannelIdleTimeout = timeout;
      return this;
    }

    /**
     * Update or insert connection flow
     *
     * @param protocol protocol such as Http, Https etc
     * @param connectionFlow defined what connection flow steps are
     *                       involved and which order they are going to be executed.
     * */
    public Builder connectionFlow(Protocol protocol, List<ConnectionFlowStep> connectionFlow) {
      _connectionFlowRegistry.put(protocol, connectionFlow);
      return this;
    }

    public ProxyServer build() {
      ProxyServer proxyServer = new ProxyServer(this);
      validate(proxyServer);
      return proxyServer;
    }

    private void validate(ProxyServer proxyServer) {
      if (proxyServer.getProxyModeControllerFactory() == null) {
        throw new IllegalStateException("proxy mode controller factory can't be null");
      }
      if (proxyServer.getConnectionFlowRegistry().isEmpty()) {
        throw new IllegalStateException("connection flow registry can't be empty");
      }
    }
  }
}
