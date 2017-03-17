/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.channel;

import com.linkedin.mitm.model.Protocol;
import com.linkedin.mitm.proxy.connectionflow.steps.ConnectionFlowStep;
import com.linkedin.mitm.proxy.factory.ErrorResponseFactory;
import com.linkedin.mitm.proxy.factory.HandlerDelegateFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 * Client channel handler that implemented read logic by
 * creating protocol specific ChannelHandlerDelegate to handle read events from client
 * It is stateful.New instance gets created every time when proxy receive a new request.
 *
 * @author shfeng
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<HttpObject> {
  private static final Logger LOG = Logger.getLogger(ClientChannelHandler.class);

  private final ChannelMediator _channelMediator;
  private final Map<Protocol, List<ConnectionFlowStep>> _connectionFlowRegistry;

  private ChannelHandlerDelegate _channelHandlerDelegate;

  public ClientChannelHandler(final ChannelMediator channelMediator,
      final Map<Protocol, List<ConnectionFlowStep>> connectionFlowRegistry) {
    _connectionFlowRegistry = connectionFlowRegistry;
    _channelMediator = channelMediator;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject)
      throws Exception {
    // initial request
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("%s: Reading from client %s", System.currentTimeMillis(), httpObject));
    }
    if (httpObject instanceof HttpRequest) {
      HttpRequest initialRequest = (HttpRequest) httpObject;
      if (_channelHandlerDelegate == null) {
        _channelHandlerDelegate =
            HandlerDelegateFactory.create(initialRequest, _channelMediator, _connectionFlowRegistry);
        _channelHandlerDelegate.onCreate();
      }
    }
    _channelHandlerDelegate.onRead(httpObject);
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx)
      throws Exception {
    _channelMediator.registerChannel(ctx.channel());
    LOG.debug("client channel registered" + ctx.channel());
    super.channelRegistered(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception {
    LOG.error("Caught exception on client channel", cause);
    _channelMediator.writeToClientAndDisconnect(ErrorResponseFactory.createInternalError(cause));
  }
}
