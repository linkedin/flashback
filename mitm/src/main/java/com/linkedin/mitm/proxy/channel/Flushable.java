/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.channel;

/**
 * Callback that give us ability to flush buffered output to server
 *
 * @author shfeng
 */
public interface Flushable {
  /**
   * Flush out HttpObjects added in the buffer
   * */
  void flush();
}
