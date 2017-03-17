/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.dataflow;

import io.netty.handler.codec.http.HttpRequest;

/**
 * Default proxy mode controller factory which create singleton
 * normal proxy controller.
 *
 * @author shfeng
 */
public class NormalProxyModeControllerFactory implements ProxyModeControllerFactory {
  @Override
  public ProxyModeController create(HttpRequest httpRequest) {
    return NormalProxyModeController.getInstance();
  }
}
