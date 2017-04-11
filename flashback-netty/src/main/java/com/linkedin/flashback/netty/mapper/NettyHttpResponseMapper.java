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
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.netty.buffer.Unpooled.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * Mapper from RecordedHttpResponse to Netty HttpResponse
 *
 * @author shfeng.
 */
public final class NettyHttpResponseMapper {

  public static final String SET_COOKIE = "Set-Cookie";

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
      // Set-Cookie headers might contains something like "Expires=Thu, 23-Mar-2017 18:01:20 GMT; Path=/"
      // It's hard to find special character to split them properly. However, we don't want encode other
      // headers becaus we don't want lose readability in the flashback.scene so let's handle Set-Cookie header
      // differently
      if (SET_COOKIE.equals(header.getKey())) {
        fullHttpResponse.headers()
            .set(header.getKey(),
                StreamSupport.stream(Splitter.onPattern(",\\s*").split(header.getValue()).spliterator(), false)
                    .map(p -> new String(Base64.getDecoder().decode(p.getBytes())))
                    .collect(Collectors.toList()));
      } else {
        fullHttpResponse.headers().set(header.getKey(), Splitter.onPattern(",\\s*").split(header.getValue()));
      }
    }
    return fullHttpResponse;
  }

  private static byte[] createHttpBodyBytes(RecordedHttpResponse recordedHttpResponse)
      throws IOException {
    return recordedHttpResponse.getHttpBody().getContent(recordedHttpResponse.getCharset());
  }
}
