/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.mapper;

import com.google.common.base.Splitter;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.io.IOException;
import java.util.Map;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * Mapper from RecordedHttpResponse to Netty HttpResponse
 *
 * @author shfeng.
 */
public final class NettyHttpResponseMapper {

  private NettyHttpResponseMapper() {
  }

  public static FullHttpResponse from(RecordedHttpResponse recordedHttpResponse)
      throws IOException {
    FullHttpResponse fullHttpResponse;
    HttpResponseStatus status = HttpResponseStatus.valueOf(recordedHttpResponse.getStatus());
    if (recordedHttpResponse.hasHttpBody()) {
      ByteBuf content = wrappedBuffer(createHttpBodyBytes(recordedHttpResponse));
      fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, status, content);
    } else {
      fullHttpResponse = new DefaultFullHttpResponse(HTTP_1_1, status);
    }
    for (Map.Entry<String, String> header : recordedHttpResponse.getHeaders().entrySet()) {
      fullHttpResponse.headers().set(header.getKey(), Splitter.onPattern(",\\s*").split(header.getValue()));
    }
    return fullHttpResponse;
  }

  private static byte[] createHttpBodyBytes(RecordedHttpResponse recordedHttpResponse)
      throws IOException {
    return recordedHttpResponse.getHttpBody().getContent(recordedHttpResponse.getCharset());
  }
}
