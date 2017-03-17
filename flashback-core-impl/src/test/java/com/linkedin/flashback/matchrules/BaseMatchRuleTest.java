/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import java.util.HashSet;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class BaseMatchRuleTest {
  @Test
  public void testInsertion() {
    Set<MatchRule> matchRuleSet = new HashSet<>();
    matchRuleSet.add(new MatchBody());
    Assert.assertEquals(matchRuleSet.size(), 1);

    matchRuleSet.add(new MatchBody());
    Assert.assertEquals(matchRuleSet.size(), 1);

    matchRuleSet.add(new MatchHeaders());
    matchRuleSet.add(MatchRuleUtils.matchHeadersWithBlacklist(null));
    matchRuleSet.add(MatchRuleUtils.matchHeadersWithWhitelist(null));
    Assert.assertEquals(matchRuleSet.size(), 4);
  }

  @Test
  public void testLookup() {
    MatchRule matchRule1 = new MatchBody();
    MatchRule matchRule2 = new MatchBody();
    MatchRule matchRule3 = new MatchHeaders();
    Set<MatchRule> matchRuleSet = new HashSet<>();
    matchRuleSet.add(matchRule1);
    Assert.assertTrue(matchRuleSet.contains(matchRule2));
    Assert.assertFalse(matchRuleSet.contains(matchRule3));
  }
}
