/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpRequest;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class CompositeMatchRuleTest {
  @Test
  public void testIsMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest("get", new URI("google.com"), null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest("get", new URI("google.com"), null, null);

    Set<MatchRule> matchRuleSet = new HashSet<MatchRule>();
    matchRuleSet.add(new MatchUri());
    matchRuleSet.add(new MatchMethod());
    CompositeMatchRule compositeMatchRule = new CompositeMatchRule();
    compositeMatchRule.addAll(matchRuleSet);

    Assert.assertTrue(compositeMatchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testIsNotMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest("get", new URI("google.com"), null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest("post", new URI("google.com"), null, null);

    Set<MatchRule> matchRuleSet = new HashSet<MatchRule>();
    matchRuleSet.add(new MatchUri());
    matchRuleSet.add(new MatchMethod());
    CompositeMatchRule compositeMatchRule = new CompositeMatchRule();
    compositeMatchRule.addAll(matchRuleSet);

    Assert.assertFalse(compositeMatchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }
}
