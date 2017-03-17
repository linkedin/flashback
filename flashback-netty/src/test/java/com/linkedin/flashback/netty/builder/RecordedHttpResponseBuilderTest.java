/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.builder;

import com.linkedin.flashback.serializable.RecordedHttpResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class RecordedHttpResponseBuilderTest {

  @Test
  public void testBuild()
      throws IOException {
    HttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.GATEWAY_TIMEOUT);
    RecordedHttpResponseBuilder recordedHttpResponseBuilder = new RecordedHttpResponseBuilder(httpResponse);

    String charset = "UTF-8";
    String str1 = "Hello world";
    HttpContent httpContent1 = new DefaultHttpContent(Unpooled.copiedBuffer(str1.getBytes(charset)));
    recordedHttpResponseBuilder.appendHttpContent(httpContent1);
    String str2 = "second content";
    HttpContent httpContent2 = new DefaultHttpContent(Unpooled.copiedBuffer(str2.getBytes(charset)));
    recordedHttpResponseBuilder.appendHttpContent(httpContent2);

    String lastStr = "Last chunk";
    HttpContent lastContent = new DefaultLastHttpContent(Unpooled.copiedBuffer(lastStr.getBytes(charset)));
    recordedHttpResponseBuilder.appendHttpContent(lastContent);
    RecordedHttpResponse recordedHttpResponse = recordedHttpResponseBuilder.build();
    Assert.assertEquals(recordedHttpResponse.getStatus(), HttpResponseStatus.GATEWAY_TIMEOUT.code());
    Assert.assertEquals((str1 + str2 + lastStr).getBytes(charset),
        recordedHttpResponse.getHttpBody().getContent(charset));
  }
}
