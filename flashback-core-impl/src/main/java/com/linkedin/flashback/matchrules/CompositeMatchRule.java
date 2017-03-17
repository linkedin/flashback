/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import java.util.HashSet;
import java.util.Set;


/**
 * Customized match rule which can contain any Match rule combinations.
 * @author shfeng
 * @author dvinegra
 */
public class CompositeMatchRule implements MatchRule {
  private Set<MatchRule> _matchRules = new HashSet<>();

  public void addRule(MatchRule matchRule) {
    _matchRules.add(matchRule);
  }

  public void addAll(Set<MatchRule> rules) {
    _matchRules.addAll(rules);
  }

  @Override
  public boolean test(final RecordedHttpRequest incomingRequest, final RecordedHttpRequest expectedRequest) {
    return Iterables.all(_matchRules, new Predicate<MatchRule>() {
      @Override
      public boolean apply(MatchRule rule) {
        return rule.test(incomingRequest, expectedRequest);
      }
    });
  }

  @Override
  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    StringBuilder resultBuilder = new StringBuilder();
    _matchRules.stream().forEach((rule) -> {
      if (!rule.test(incomingRequest, expectedRequest)) {
        resultBuilder.append(rule.getMatchFailureDescriptionForRequests(incomingRequest, expectedRequest)).append("\n");
      }
    });
    return resultBuilder.toString();
  }
}
