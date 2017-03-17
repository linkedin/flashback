/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.channel;

import io.netty.handler.codec.http.HttpObject;


/**
 * Interface to create Handler delegate which will
 * be used by {@Link ClientChannelHandler}
 *
 * @author shfeng .
 */
public interface ChannelHandlerDelegate {
  /**
   * Initializes before we can read data.
   * */
  void onCreate();

  /**
   * Read HttpObject
   * */
  void onRead(HttpObject httpObject);
}
