/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpRequest;


/**
 * Match rule to match http headers
 * @author shfeng.
 */
public class MatchHeaders extends BaseMatchRule {

  private final MatchRuleMapTransform _transform;

  public MatchHeaders() {
    this(null);
  }

  public MatchHeaders(MatchRuleMapTransform transform) {
    if (transform != null) {
      _transform = transform;
    } else {
      _transform = new MatchRuleIdentityTransform();
    }
  }

  @Override
  public boolean test(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    return _transform.transform(incomingRequest.getHeaders())
        .equals(_transform.transform(expectedRequest.getHeaders()));
  }

  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    StringBuilder resultBuilder = new StringBuilder("HTTP Headers Mismatch");
    if (_transform instanceof MatchRuleBlacklistTransform) {
      resultBuilder.append(" (with Blacklist)");
    } else if (_transform instanceof MatchRuleWhitelistTransform) {
      resultBuilder.append(" (with Whitelist)");
    }
    resultBuilder.append("%n")
        .append(String.format("Incoming Headers: %s%n", _transform.transform(incomingRequest.getHeaders())))
        .append(String.format("Expected Headers: %s%n", _transform.transform(expectedRequest.getHeaders())));
    return resultBuilder.toString();
  }
}
