/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.builder;

import com.linkedin.flashback.serializable.RecordedHttpRequest;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class RecordedHttpRequestBuilderTest {

  @Test
  public void testBuildHttpMethod() {
    HttpRequest nettyRequest =
        new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "http://www.google.com");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    RecordedHttpRequest recordedHttpRequest = recordedHttpRequestBuilder.build();
    Assert.assertEquals(recordedHttpRequest.getMethod(), HttpMethod.GET.toString());
  }

  @Test
  public void testBuildAbsoluteUri() {
    String uri = "http://www.google.com";
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, uri);
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    RecordedHttpRequest recordedHttpRequest = recordedHttpRequestBuilder.build();
    Assert.assertEquals(recordedHttpRequest.getUri().toString(), uri);
  }

  @Test
  public void testBuildRelativeUri() {
    String uri = "finance";
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, uri);
    nettyRequest.headers().set(HttpHeaders.Names.HOST, "www.google.com/");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    RecordedHttpRequest recordedHttpRequest = recordedHttpRequestBuilder.build();
    Assert.assertEquals(recordedHttpRequest.getUri().toString(), "https://www.google.com/finance");
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildWithUnsupportedUri() {
    String uri = "http://example.com/file[/].html";
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, uri);
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    recordedHttpRequestBuilder.build();
  }

  @Test
  public void testBuildWithUriTwoLegs() {
    HttpRequest nettyRequest1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "finance");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest1);
    HttpRequest nettyRequest2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "google.com");
    nettyRequest2.headers().set(HttpHeaders.Names.HOST, "www.google.com/");
    recordedHttpRequestBuilder.addHeaders(nettyRequest2);
    RecordedHttpRequest recordedHttpRequest = recordedHttpRequestBuilder.build();
    Assert.assertEquals(recordedHttpRequest.getUri().toString(), "https://www.google.com/finance");
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testBuildWithUriTwoLegsIllegalUri() {
    HttpRequest nettyRequest1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "file[/].html");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest1);
    HttpRequest nettyRequest2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "google.com");
    nettyRequest2.headers().set(HttpHeaders.Names.HOST, "www.google.com/");
    recordedHttpRequestBuilder.addHeaders(nettyRequest2);
    recordedHttpRequestBuilder.build();
  }

  @Test
  public void testBuildContent()
      throws Exception {
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "www.google.com");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);

    String charset = "UTF-8";
    String str1 = "first content";
    HttpContent httpContent1 = new DefaultHttpContent(Unpooled.copiedBuffer(str1.getBytes(charset)));
    recordedHttpRequestBuilder.appendHttpContent(httpContent1);
    String str2 = "second content";
    HttpContent httpContent2 = new DefaultHttpContent(Unpooled.copiedBuffer(str2.getBytes(charset)));
    recordedHttpRequestBuilder.appendHttpContent(httpContent2);

    String lastStr = "Last chunk";
    HttpContent lastContent = new DefaultLastHttpContent(Unpooled.copiedBuffer(lastStr.getBytes(charset)));
    recordedHttpRequestBuilder.appendHttpContent(lastContent);

    RecordedHttpRequest recordedHttpRequest = recordedHttpRequestBuilder.build();
    Assert
        .assertEquals((str1 + str2 + lastStr).getBytes(charset), recordedHttpRequest.getHttpBody().getContent(charset));
  }
}
