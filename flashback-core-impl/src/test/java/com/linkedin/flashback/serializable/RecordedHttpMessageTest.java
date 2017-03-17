/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class RecordedHttpMessageTest {
  @Test
  public void testGetCharset()
      throws URISyntaxException {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.CONTENT_TYPE, "text/html; charset=iso-8859-9");
    RecordedHttpRequest recordedHttpRequest = new RecordedHttpRequest("GET", new URI("google.com"), headers, null);
    Assert.assertEquals(recordedHttpRequest.getCharset(), Charset.forName("iso-8859-9").toString());
  }

  @Test
  public void testGetCharsetNoContentType()
      throws URISyntaxException {
    Map<String, String> headers = new HashMap<>();
    RecordedHttpRequest recordedHttpRequest = new RecordedHttpRequest("GET", new URI("google.com"), headers, null);
    Assert.assertEquals(recordedHttpRequest.getCharset(), Charsets.UTF_8.toString());
  }

  @Test
  public void testPassNullHeaders()
      throws URISyntaxException {
    RecordedHttpRequest recordedHttpRequest = new RecordedHttpRequest("GET", new URI("google.com"), null, null);
    Assert.assertNull(recordedHttpRequest.getHeaders().get("anykey"));
  }

  @Test
  public void testGetContentType()
      throws URISyntaxException {
    Map<String, String> headers = new HashMap<>();
    headers.put(HttpHeaders.CONTENT_TYPE, "text/html");
    RecordedHttpRequest recordedHttpRequest = new RecordedHttpRequest("GET", new URI("google.com"), headers, null);
    Assert.assertEquals(recordedHttpRequest.getContentType(), "text/html");
  }
}
