/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpRequest;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class MatchMethodTest {
  @Test
  public void testIsMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest("GET", null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest("GET", null, null, null);
    MatchRule matchRule = new MatchMethod();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testIsNotMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest("GET", null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest("get", null, null, null);
    MatchRule matchRule = new MatchMethod();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }
}
