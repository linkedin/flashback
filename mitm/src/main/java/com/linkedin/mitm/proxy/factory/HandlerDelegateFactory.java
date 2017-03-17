/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.factory;

import com.linkedin.mitm.model.Protocol;
import com.linkedin.mitm.proxy.channel.ChannelHandlerDelegate;
import com.linkedin.mitm.proxy.channel.ChannelMediator;
import com.linkedin.mitm.proxy.channel.protocol.HttpChannelHandlerDelegate;
import com.linkedin.mitm.proxy.channel.protocol.HttpsChannelHandlerDelegate;
import com.linkedin.mitm.proxy.connectionflow.ConnectionFlowProcessor;
import com.linkedin.mitm.proxy.connectionflow.steps.ConnectionFlowStep;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import java.util.List;
import java.util.Map;


/**
 * Create Protocol specific handler delegate
 *
 * @author shfeng
 */
public class HandlerDelegateFactory {
  public static ChannelHandlerDelegate create(HttpRequest httpRequest, ChannelMediator channelMediator,
      Map<Protocol, List<ConnectionFlowStep>> connectionFlowRegistry) {
    if (HttpMethod.CONNECT.equals(httpRequest.getMethod())) {
      List<ConnectionFlowStep> connectionFlow = connectionFlowRegistry.get(Protocol.HTTPS);
      ConnectionFlowProcessor httpsConnectionFlowProcessor =
          new ConnectionFlowProcessor(channelMediator, httpRequest, connectionFlow);
      channelMediator.initializeProxyModeController(httpRequest);
      return new HttpsChannelHandlerDelegate(channelMediator, httpsConnectionFlowProcessor);
    } else {
      List<ConnectionFlowStep> connectionFlow = connectionFlowRegistry.get(Protocol.HTTP);
      ConnectionFlowProcessor httpConnectionFlowProcessor =
          new ConnectionFlowProcessor(channelMediator, httpRequest, connectionFlow);
      channelMediator.initializeProxyModeController(httpRequest);
      return new HttpChannelHandlerDelegate(channelMediator, httpConnectionFlowProcessor);
    }
  }

  private HandlerDelegateFactory() {

  }
}
