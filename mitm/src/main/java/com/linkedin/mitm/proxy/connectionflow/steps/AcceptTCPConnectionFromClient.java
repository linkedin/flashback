/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.connectionflow.steps;

import com.linkedin.mitm.proxy.channel.ChannelMediator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import org.apache.log4j.Logger;

/**
 * This class is used to finish establishing TCP connection with client.
 *
 * @author shfeng
 */
public class AcceptTCPConnectionFromClient implements ConnectionFlowStep {
  private static final String MODULE = AcceptTCPConnectionFromClient.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private static final HttpResponseStatus CONNECTION_ESTABLISHED =
      new HttpResponseStatus(200, "HTTP/1.1 200 Connection established");

  @Override
  public Future execute(ChannelMediator channelMediator, InetSocketAddress remoteAddress) {
    LOG.debug("Returning 200 response to client to indicate connection established");
    HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, CONNECTION_ESTABLISHED);
    return channelMediator.writeToClient(response);
  }
}
