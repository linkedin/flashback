/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.http;

import java.net.URI;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author dvinegra
 */
public class HttpUtilitiesTest {

  @Test
  public void testIsTextContentType() {
    Assert.assertTrue(HttpUtilities.isTextContentType("application/json"));
    Assert.assertTrue(HttpUtilities.isTextContentType("text/javascript"));
    Assert.assertTrue(HttpUtilities.isTextContentType("text/x-javascript"));
    Assert.assertTrue(HttpUtilities.isTextContentType("text/x-json"));
    Assert.assertTrue(HttpUtilities.isTextContentType("text/html"));
    Assert.assertTrue(HttpUtilities.isTextContentType("application/xhtml+xml"));
    Assert.assertTrue(HttpUtilities.isTextContentType("text/xml"));
    Assert.assertTrue(HttpUtilities.isTextContentType("application/xml"));
    Assert.assertTrue(HttpUtilities.isTextContentType("application/x-www-form-urlencoded"));
    Assert.assertFalse(HttpUtilities.isTextContentType("image/gif"));
    Assert.assertFalse(HttpUtilities.isTextContentType(null));
  }

  @Test
  public void testIsCompressedContentEncoding() {
    Assert.assertTrue(HttpUtilities.isCompressedContentEncoding("gzip"));
    Assert.assertTrue(HttpUtilities.isCompressedContentEncoding("deflate"));
    Assert.assertFalse(HttpUtilities.isCompressedContentEncoding("identity"));
  }

  @Test
  public void isFormURLEncodedContentType() {
    Assert.assertTrue(HttpUtilities.isFormURLEncodedContentType("application/x-www-form-urlencoded"));
    Assert.assertFalse(HttpUtilities.isFormURLEncodedContentType("application/x-javascript"));
  }

  @Test
  public void testStringUrlParameterConversion()
      throws Exception {
    String queryString = "foo=bar&a=a&b=b&c=c";
    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("foo", "bar");
    expected.put("a", "a");
    expected.put("b", "b");
    expected.put("c", "c");
    Assert.assertEquals(HttpUtilities.stringToUrlParams(queryString, "UTF-8"), expected);
    Assert.assertEquals(HttpUtilities.urlParamsToString(expected, "UTF-8"), queryString);
    Assert.assertEquals(HttpUtilities.stringToUrlParams(queryString, "UTF-8").toString(), expected.toString());
  }

  @Test
  public void testStringUrlParameterConversionWithNestedUrl()
      throws Exception {
    String nestedUriString = "http://www.google.com/?a=b";
    URI uri = new URI("http://www.example.org/?foo=bar&ref=" + URLEncoder.encode(nestedUriString, "UTF-8"));
    Map<String, String> expected = new LinkedHashMap<>();
    expected.put("foo", "bar");
    expected.put("ref", nestedUriString);
    Map<String, String> result = HttpUtilities.stringToUrlParams(uri.getRawQuery(), "UTF-8");
    Assert.assertEquals(result, expected);
    Assert.assertEquals(HttpUtilities.urlParamsToString(result, "UTF-8"), uri.getRawQuery());
  }

  @Test
  public void testStringUrlParameterConversionWrongOrder()
      throws Exception {
    String queryString = "foo=bar&a=a&b=b&c=c";
    Map<String, String> expectedOutOfOrder = new LinkedHashMap<>();
    expectedOutOfOrder.put("a", "a");
    expectedOutOfOrder.put("b", "b");
    expectedOutOfOrder.put("c", "c");
    expectedOutOfOrder.put("foo", "bar");
    Assert.assertNotEquals(HttpUtilities.urlParamsToString(expectedOutOfOrder, "UTF-8"), queryString);
    Assert.assertNotEquals(HttpUtilities.stringToUrlParams(queryString, "UTF-8").toString(),
        expectedOutOfOrder.toString());
  }
}
