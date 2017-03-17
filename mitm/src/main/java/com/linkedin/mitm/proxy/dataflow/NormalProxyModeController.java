/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.dataflow;

import com.linkedin.mitm.proxy.channel.ChannelMediator;
import io.netty.handler.codec.http.HttpObject;


/**
 * Normal ProxyModeController that proxy request from client to server and
 * from server to client.
 *
 * @author shfeng
 */
public class NormalProxyModeController implements ProxyModeController {
  private static NormalProxyModeController _normalProxyModeController = new NormalProxyModeController();

  public static NormalProxyModeController getInstance() {
    return _normalProxyModeController;
  }

  private NormalProxyModeController() {
  }

  @Override
  public void handleReadFromClient(final ChannelMediator channelMediator, final HttpObject httpObject) {
    if (channelMediator != null) {
      channelMediator.writeToServer(httpObject);
    }
  }
}
