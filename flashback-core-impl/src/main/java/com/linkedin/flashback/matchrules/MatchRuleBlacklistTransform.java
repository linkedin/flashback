/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Transform that returns a map containing only the keys absent from the blacklist
 * @author dvinegra
 */
public class MatchRuleBlacklistTransform implements MatchRuleMapTransform {

  private final Set<String> _blackList;

  public MatchRuleBlacklistTransform(Set<String> blackList) {
    if (blackList != null) {
      _blackList = blackList;
    } else {
      _blackList = Collections.EMPTY_SET;
    }
  }

  @Override
  public Map<String, String> transform(Map<String, String> map) {
    return map.keySet().stream().filter(k -> !_blackList.contains(k))
        .collect(Collectors.toMap(k -> k, k -> map.get(k), (k, v) -> {
          throw new RuntimeException("Duplicate key " + k);
        }, LinkedHashMap::new));
  }
}
