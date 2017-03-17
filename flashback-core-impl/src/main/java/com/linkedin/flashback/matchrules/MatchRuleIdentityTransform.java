/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Transform that returns a copy of the existing map
 * @author dvinegra
 */
public class MatchRuleIdentityTransform implements MatchRuleMapTransform {

  @Override
  public Map<String, String> transform(Map<String, String> map) {
    return new LinkedHashMap<>(map);
  }
}
