/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.dataflow;

import io.netty.handler.codec.http.HttpRequest;

/**
 * ProxyModeController factory which is used for generate either default
 * Normal proxy controller or customized proxy controllers.
 *
 * @author shfeng
 */
public interface ProxyModeControllerFactory {
  ProxyModeController create(HttpRequest httpRequest);
}
