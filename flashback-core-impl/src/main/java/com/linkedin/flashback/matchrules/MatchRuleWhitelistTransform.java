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
 * Transform that returns a map containing only the keys in the whitelist
 * @author dvinegra
 */
public class MatchRuleWhitelistTransform implements MatchRuleMapTransform {

  private final Set<String> _whiteList;

  public MatchRuleWhitelistTransform(Set<String> whiteList) {
    if (whiteList != null) {
      _whiteList = whiteList;
    } else {
      _whiteList = Collections.EMPTY_SET;
    }
  }

  @Override
  public Map<String, String> transform(Map<String, String> map) {
    return map.keySet().stream().filter(_whiteList::contains)
        .collect(Collectors.toMap(k -> k, k -> map.get(k), (k, v) -> {
          throw new RuntimeException("Duplicate key " + k);
        }, LinkedHashMap::new));
  }
}
