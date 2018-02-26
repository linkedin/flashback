/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.mapper;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import io.netty.handler.codec.http.FullHttpResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class NettyHttpResponseMapperTest {
  @Test
  public void testFromWithoutBody()
      throws Exception {
    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("key1", "value1");
    headers.put("key2", "value2,value3,value4");
    int status = 200;
    RecordedHttpResponse recordedHttpResponse = new RecordedHttpResponse(status, headers, null);
    FullHttpResponse fullHttpResponse = NettyHttpResponseMapper.from(recordedHttpResponse);
    Assert.assertEquals(fullHttpResponse.getStatus().code(), status);
    Assert.assertEquals(fullHttpResponse.headers().get("key1"), "value1");
    List<String> headrValues = fullHttpResponse.headers().getAll("key2");
    Assert.assertEquals(headrValues.size(), 1);
    Assert.assertTrue(headrValues.contains("value2,value3,value4"));
  }

  @Test
  public void testFromWithBody()
      throws Exception {
    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("key1", "value1");
    headers.put("key2", "value2,value3,value4");
    int status = 200;
    String str = "Hello world";
    RecordedStringHttpBody recordedStringHttpBody = new RecordedStringHttpBody(str);
    RecordedHttpResponse recordedHttpResponse = new RecordedHttpResponse(status, headers, recordedStringHttpBody);
    FullHttpResponse fullHttpResponse = NettyHttpResponseMapper.from(recordedHttpResponse);
    Assert.assertEquals(fullHttpResponse.getStatus().code(), status);
    Assert.assertEquals(fullHttpResponse.headers().get("key1"), "value1");
    List<String> headrValues = fullHttpResponse.headers().getAll("key2");
    Assert.assertEquals(headrValues.size(), 1);
    Assert.assertTrue(headrValues.contains("value2,value3,value4"));
    Assert.assertEquals(fullHttpResponse.content().array(), str.getBytes());
  }

  @Test
  public void testCookieHeader() throws URISyntaxException, IOException {
    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("key1", "value1");
    headers.put("Set-Cookie", "YSxiLGM=, ZCxlLGY=");
    int status = 200;
    String str = "Hello world";
    RecordedStringHttpBody recordedStringHttpBody = new RecordedStringHttpBody(str);

    RecordedHttpResponse recordedHttpResponse = new RecordedHttpResponse(status, headers, recordedStringHttpBody);
    FullHttpResponse fullHttpResponse = NettyHttpResponseMapper.from(recordedHttpResponse);
    Assert.assertEquals(fullHttpResponse.getStatus().code(), status);
    Assert.assertEquals(fullHttpResponse.headers().get("key1"), "value1");
    List<String> headrValues = fullHttpResponse.headers().getAll("Set-Cookie");
    Assert.assertEquals(headrValues.size(), 1);
    Assert.assertTrue(headrValues.contains("YSxiLGM=, ZCxlLGY="));
  }

  @Test
  public void testNonCookieHeader() throws URISyntaxException, IOException {
    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("key1", "value1");
    headers.put("Not-Set-Cookie", "YSxiLGM=, ZCxlLGY=");
    int status = 200;
    String str = "Hello world";
    RecordedStringHttpBody recordedStringHttpBody = new RecordedStringHttpBody(str);

    RecordedHttpResponse recordedHttpResponse = new RecordedHttpResponse(status, headers, recordedStringHttpBody);
    FullHttpResponse fullHttpResponse = NettyHttpResponseMapper.from(recordedHttpResponse);
    Assert.assertEquals(fullHttpResponse.getStatus().code(), status);
    Assert.assertEquals(fullHttpResponse.headers().get("key1"), "value1");
    List<String> headrValues = fullHttpResponse.headers().getAll("Not-Set-Cookie");
    Assert.assertEquals(headrValues.size(), 1);
    Assert.assertTrue(headrValues.contains("YSxiLGM=, ZCxlLGY="));
  }

  @Test
  public void testDuplicateHeader() throws IOException {
    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("key1", "value1");
    headers.put("key1", "value2");
    int status = 200;
    RecordedHttpResponse recordedHttpResponse = new RecordedHttpResponse(status, headers, null);
    FullHttpResponse fullHttpResponse = NettyHttpResponseMapper.from(recordedHttpResponse);
    Assert.assertEquals(fullHttpResponse.getStatus().code(), status);
    List<String> headrValues = fullHttpResponse.headers().getAll("key1");
    Assert.assertEquals(headrValues.size(), 2);
    Assert.assertTrue(headrValues.contains("value1"));
    Assert.assertTrue(headrValues.contains("value2"));
  }
}
