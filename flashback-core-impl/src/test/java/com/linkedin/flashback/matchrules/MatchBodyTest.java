/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.google.common.net.HttpHeaders;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class MatchBodyTest {
  @Test
  public void testIsStringBodyMatch()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("abc");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("abc");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBody();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testBothNullBodyMatch() {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, null);
    MatchRule matchRule = new MatchBody();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNullAndEmptyBodyMatch() {
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBody();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNullAndNonEmptyBodyNotMatch() {
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("abc");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBody();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testIsStringBodyNotMatch()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("abc");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("abcd");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBody();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testIsStringBodyNotMatchWithDiffCharset()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("造字");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("造字");
    Map<String, String> headers1 = new HashMap<>();
    headers1.put(HttpHeaders.CONTENT_TYPE, "text/html; charset=euc-kr");
    Map<String, String> headers2 = new HashMap<>();
    headers2.put(HttpHeaders.CONTENT_TYPE, "text/html; charset=big5");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, stringHttpBody2);
    MatchRule matchRule = new MatchBody();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }
}
