/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.factory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;


/**
 * Create various error response
 *
 * @author shfeng
 */
public class ErrorResponseFactory {

  public static FullHttpResponse createInternalError(Throwable throwable) {
    ByteBuf badRequestBody =
        Unpooled.wrappedBuffer(throwable.getMessage().getBytes(Charset.forName("UTF-8")));
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, badRequestBody);
  }

  private ErrorResponseFactory() {
  }
}
