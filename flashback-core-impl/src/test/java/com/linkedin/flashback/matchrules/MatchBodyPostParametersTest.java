/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.util.HashMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author dvinegra
 */
public class MatchBodyPostParametersTest {

  @Test
  public void testExactMatch()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=b&c=c");

    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = new MatchBodyPostParameters();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNotMatchDifferentOrder()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&c=c&b=b");

    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = new MatchBodyPostParameters();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testDifferentParameters()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=b&c=ccc");

    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = new MatchBodyPostParameters();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testMatchWithSomeNullParameters()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=&c=c&d=");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=&c=c&d=");

    Multimap<String, String> headers = LinkedHashMultimap.create();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = new MatchBodyPostParameters();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }
}
