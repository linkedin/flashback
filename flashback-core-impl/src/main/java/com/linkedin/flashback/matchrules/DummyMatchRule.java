/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpRequest;


/**
 * Create a dummy match rule for proxy server to start with, then
 * the user will need to set their specific rule as need.
 */
public class DummyMatchRule extends BaseMatchRule {
  static final String MATCH_RULE_IS_NOT_VALID = "match rule is not valid";

  @Override
  public boolean test(RecordedHttpRequest recordedHttpRequest, RecordedHttpRequest recordedHttpRequest2) {
    throw new IllegalStateException(MATCH_RULE_IS_NOT_VALID);
  }

  @Override
  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    throw new IllegalStateException(MATCH_RULE_IS_NOT_VALID);
  }
}
