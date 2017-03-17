/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.connectionflow.steps;

import com.linkedin.mitm.proxy.channel.ChannelMediator;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import org.apache.log4j.Logger;


/**
 * Resume reading from client
 *
 * @author shfeng
 */
public class ResumeReadingFromClient implements ConnectionFlowStep {
  private static final String MODULE = ResumeReadingFromClient.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);

  @Override
  public Future execute(ChannelMediator channelMediator, InetSocketAddress remoteAddress) {
    LOG.debug("Resume reading from client");
    return channelMediator.resumeReadingFromClientChannel();
  }
}
