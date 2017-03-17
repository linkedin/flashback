/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.mapper;

import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import io.netty.handler.codec.http.FullHttpResponse;
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
    Map<String, String> headers = new HashMap<>();
    headers.put("key1", "value1");
    headers.put("key2", "value2,value3,value4");
    int status = 200;
    RecordedHttpResponse recordedHttpResponse = new RecordedHttpResponse(status, headers, null);
    FullHttpResponse fullHttpResponse = NettyHttpResponseMapper.from(recordedHttpResponse);
    Assert.assertEquals(fullHttpResponse.getStatus().code(), status);
    Assert.assertEquals(fullHttpResponse.headers().get("key1"), "value1");
    List<String> headrValues = fullHttpResponse.headers().getAll("key2");
    Assert.assertEquals(headrValues.size(), 3);
    Assert.assertTrue(headrValues.contains("value2"));
    Assert.assertTrue(headrValues.contains("value3"));
    Assert.assertTrue(headrValues.contains("value4"));
  }

  @Test
  public void testFromWithBody()
      throws Exception {
    Map<String, String> headers = new HashMap<>();
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
    Assert.assertEquals(headrValues.size(), 3);
    Assert.assertTrue(headrValues.contains("value2"));
    Assert.assertTrue(headrValues.contains("value3"));
    Assert.assertTrue(headrValues.contains("value4"));
    Assert.assertEquals(fullHttpResponse.content().array(), str.getBytes());
  }
}
