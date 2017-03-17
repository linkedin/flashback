/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

/**
 * List out of pre-defined match rules. It's mainly used for non-java
 * application because flashback doesn't support non-java match rules
 * now.
 *
 * @author shfeng
 */
public enum NamedMatchRule {
  MATCH_ENTIRE_REQUEST("matchEntireRequest"),
  MATCH_METHOD_URI("matchMethodUri"),
  MATCH_METHOD_BODY_URI("matchMethodBodyUri");
  private final String _text;

  NamedMatchRule(String text) {
    _text = text;
  }

  public static MatchRule fromString(String predefinedMatchRule) {
    if (MATCH_ENTIRE_REQUEST._text.equalsIgnoreCase(predefinedMatchRule)) {
      return MatchRuleUtils.matchEntireRequest();
    }
    if (MATCH_METHOD_URI._text.equalsIgnoreCase(predefinedMatchRule)) {
      return MatchRuleUtils.matchMethodUri();
    }
    if (MATCH_METHOD_BODY_URI._text.equalsIgnoreCase(predefinedMatchRule)) {
      return MatchRuleUtils.matchMethodBodyUri();
    }
    return null;
  }
}
