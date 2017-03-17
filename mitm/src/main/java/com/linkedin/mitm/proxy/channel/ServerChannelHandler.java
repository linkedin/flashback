/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.channel;

import com.linkedin.mitm.proxy.factory.ErrorResponseFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpObject;
import org.apache.log4j.Logger;

/**
 * Server channel handler that implemented read logic from server side.
 * Note: It's stateful. Each {@link com.linkedin.mitm.proxy.channel.ClientChannelHandler} map to one
 * ServerChannelHandler.
 *
 * @author shfeng
 */
public class ServerChannelHandler extends SimpleChannelInboundHandler<HttpObject> {
  private static final String MODULE = ServerChannelHandler.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);

  private final ChannelMediator _channelMediator;

  public ServerChannelHandler(ChannelMediator channelMediator) {
    _channelMediator = channelMediator;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject)
      throws Exception {
    _channelMediator.readFromServerChannel(httpObject);
    if (httpObject instanceof DefaultLastHttpContent) {
      _channelMediator.writeToClientAndDisconnect(httpObject);
    } else {
      _channelMediator.writeToClient(httpObject);
    }
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx)
      throws Exception {
    _channelMediator.registerChannel(ctx.channel());
    LOG.debug("server channel registered" + ctx.channel());
    super.channelRegistered(ctx);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception {
    LOG.error("Caught exception on server channel", cause);
    _channelMediator.writeToClientAndDisconnect(ErrorResponseFactory.createInternalError(cause));
  }
}
