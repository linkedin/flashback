/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.smartproxy.utils;

import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;


/**
 * Util class to generate response to indicate request has no match in the scene
 */
public class NoMatchResponseGenerator {
  /*
   * Builds the 400/Bad Request response to return when there is no matching request
   */
  public static FullHttpResponse generateNoMatchResponse(RecordedHttpRequest recordedHttpRequest) {
    StringBuilder bodyTextBuilder = new StringBuilder();
    bodyTextBuilder.append("No Matching Request\n").append("Incoming Request Method: ")
        .append(recordedHttpRequest.getMethod()).append("\n").append("Incoming Request URI: ")
        .append(recordedHttpRequest.getUri()).append("\n").append("Incoming Request Headers: ")
        .append(recordedHttpRequest.getHeaders()).append("\n");
    RecordedHttpBody incomingBody = recordedHttpRequest.getHttpBody();
    if (incomingBody != null) {
      if (incomingBody instanceof RecordedEncodedHttpBody) {
        incomingBody = ((RecordedEncodedHttpBody) incomingBody).getDecodedBody();
      }
      if (incomingBody instanceof RecordedStringHttpBody) {
        bodyTextBuilder.append("Incoming Request Body: ").append(((RecordedStringHttpBody) incomingBody).getContent());
      } else {
        bodyTextBuilder.append("Incoming Request Body: (binary content)");
      }
    }
    ByteBuf badRequestBody = Unpooled.wrappedBuffer(bodyTextBuilder.toString().getBytes(Charset.forName("UTF-8")));
    return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, badRequestBody);
  }

  private NoMatchResponseGenerator() {
  }
}
