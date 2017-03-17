/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.connectionflow.steps;

import com.linkedin.mitm.proxy.channel.ChannelMediator;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;


/**
 * Represent callback for each connection flow step
 * Note: Customized connection flow steps need to be thread safe
 *
 * @author shfeng
 */
public interface ConnectionFlowStep {
  /**
   * Implement this method to actually do the work involved in this step of
   * the flow.
   *
   * @param channelMediator client to proxy channel handler
   * @param remoteAddress remote address to upstream service
   *
   */
  Future execute(ChannelMediator channelMediator, InetSocketAddress remoteAddress);
}
