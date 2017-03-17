/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpRequest;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class MatchHeadersTest {
  @Test
  public void testIsMatch()
      throws Exception {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);
    MatchRule matchRule = new MatchHeaders();

    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNotMatchWrongKey()
      throws Exception {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key3", "value2");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);
    MatchRule matchRule = new MatchHeaders();

    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNotMatchWrongValue()
      throws Exception {
    Map<String, String> headers1 = new HashMap<String, String>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<String, String>();
    headers2.put("key1", "value1");
    headers2.put("key2", "value1");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);
    MatchRule matchRule = new MatchHeaders();

    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testMatchDifferentOrder()
      throws Exception {
    Map<String, String> headers1 = new HashMap<String, String>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<String, String>();
    headers2.put("key2", "value2");
    headers2.put("key1", "value1");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);
    MatchRule matchRule = new MatchHeaders();

    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNotMatchWrongSize()
      throws Exception {
    Map<String, String> headers1 = new HashMap<String, String>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<String, String>();
    headers2.put("key1", "value1");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);
    MatchRule matchRule = new MatchHeaders();

    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNotMatchEmptyMap()
      throws Exception {
    Map<String, String> headers1 = new HashMap<String, String>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<String, String>();
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);
    MatchRule matchRule = new MatchHeaders();

    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }
}
