/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.connectionflow.steps;

import com.linkedin.mitm.proxy.channel.ChannelMediator;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;
import org.apache.log4j.Logger;

/**
 * Start handshaking with server. It behaves as client and handshake with server.
 *
 * @author shfeng
 */
public class HandshakeWithServer implements ConnectionFlowStep {
  private static final String MODULE = HandshakeWithServer.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private final SSLContext _sslContext;

  public HandshakeWithServer(SSLContext sslContext) {
    _sslContext = sslContext;
  }

  @Override
  public Future execute(ChannelMediator channelMediator, InetSocketAddress remoteAddress) {
    LOG.debug("Starting proxy to server connection handshaking");
    return channelMediator.handshakeWithServer(_sslContext.createSSLEngine());
  }
}
