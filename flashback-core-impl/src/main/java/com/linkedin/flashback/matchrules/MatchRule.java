/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpRequest;
import java.util.function.BiPredicate;


/**
 * Interface for Match Rule
 * @author shfeng
 */
public interface MatchRule extends BiPredicate<RecordedHttpRequest, RecordedHttpRequest> {

  /**
   * Returns a description for the match failure for a pair of requests
   * @return the match failure description string
   */
  String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest);
}
