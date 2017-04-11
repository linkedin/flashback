/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.builder;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URISyntaxException;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class RecordedHttpMessageBuilderTest {
  @Test
  public void testAddHeaders()
      throws Exception {
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "www.google.com");
    nettyRequest.headers().add("key1", "value1");
    nettyRequest.headers().add("key1", "value2");
    nettyRequest.headers().add("key2", "value1");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    Map<String, String> headers = recordedHttpRequestBuilder.getHeaders();
    Assert.assertEquals(headers.size(), 2);
  }

  @Test
  public void testGetHeaders()
      throws Exception {
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "www.google.com");
    nettyRequest.headers().add("key1", "value1");
    nettyRequest.headers().add("key1", "value2");
    nettyRequest.headers().add("key2", "value1");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    Map<String, String> headers = recordedHttpRequestBuilder.getHeaders();
    Assert.assertEquals(headers.size(), 2);
    Assert.assertEquals(headers.get("key1"), "value1, value2");
    Assert.assertEquals(headers.get("key2"), "value1");
  }

  @Test
  public void testSetCookieHeader() throws URISyntaxException {
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "www.abc.com");
    nettyRequest.headers()
        .add("Set-Cookie",
            "a,b,c");
    nettyRequest.headers()
        .add("Set-Cookie",
            "d,e,f");
    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    Map<String, String> headers = recordedHttpRequestBuilder.getHeaders();

    Assert.assertEquals(headers.size(), 1);
    Assert.assertEquals(headers.get("Set-Cookie"), "YSxiLGM=, ZCxlLGY=");
  }

  @Test
  public void testNonSetCookieHeader() throws URISyntaxException {
    HttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, "www.abc.com");
    nettyRequest.headers()
        .add("Not-Set-Cookie",
            "a,b,c");
    nettyRequest.headers()
        .add("Not-Set-Cookie",
            "d,e,f");

    RecordedHttpRequestBuilder recordedHttpRequestBuilder = new RecordedHttpRequestBuilder(nettyRequest);
    Map<String, String> headers = recordedHttpRequestBuilder.getHeaders();

    Assert.assertEquals(headers.size(), 1);
    Assert.assertNotEquals(headers.get("Not-Set-Cookie"), "YSxiLGM=, ZCxlLGY=");
  }
}
