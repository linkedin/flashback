/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.channel.protocol;

import com.linkedin.mitm.proxy.channel.ChannelHandlerDelegate;
import com.linkedin.mitm.proxy.channel.ChannelMediator;
import com.linkedin.mitm.proxy.channel.Flushable;
import com.linkedin.mitm.proxy.connectionflow.ConnectionFlowProcessor;
import io.netty.handler.codec.http.HttpObject;
import org.apache.log4j.Logger;


/**
 * Https specific logic to handle reading from Client side
 * Note: The https request flow can be explicitly break down to two parts:
 * 1. Client send Connect Http Request. When netty framework process request, it fire two read events which
 * break to two parts: {@link io.netty.handler.codec.http.HttpRequest} and {@link io.netty.handler.codec.http.LastHttpContent}
 * Since mitm proxy just need create a another TCP connection to server, we could drop the initial request.
 * 2. After connection is built, we could read real HttpObject and forward to server.
 *
 * @author shfeng
 */
public class HttpsChannelHandlerDelegate implements ChannelHandlerDelegate {
  private static final String MODULE = HttpsChannelHandlerDelegate.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private final ConnectionFlowProcessor _connectionFlowProcessor;
  private final ChannelMediator _channelMediator;

  public HttpsChannelHandlerDelegate(ChannelMediator channelMediator,
      ConnectionFlowProcessor httpsConnectionFlowProcessor) {
    _channelMediator = channelMediator;
    _connectionFlowProcessor = httpsConnectionFlowProcessor;
  }

  @Override
  public void onCreate() {
    _connectionFlowProcessor.startConnectionFlow(new Flushable() {
      @Override
      public void flush() {

      }
    });
  }

  @Override
  public void onRead(HttpObject httpObject) {
    // Drop initial Connection request.
    // Basically,CONNECT request will be sliced to two parts: HttpRequest and LastContent(empty content to indicate end of
    // this request). This HttpRequest is only used to indicates that it's time to build tunnel between client and server.
    // It invokes Connection flow to build two tunnels: between client to proxy and proxy to server.
    // After that, both HttpRequest and HttpLastContent is useless, that's why they are get dropped.
    // Only after connection flow is complete, we start reading and processing incoming request from client
    if (_connectionFlowProcessor.isComplete()) {
      _channelMediator.readFromClientChannel(httpObject);
    }
  }
}
