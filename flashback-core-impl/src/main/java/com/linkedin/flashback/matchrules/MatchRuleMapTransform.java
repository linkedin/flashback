/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import java.util.Map;


/**
 * Interface to perform a transformation on a Map, which is a common operation of several Match Rules
 * @author dvinegra
 */
public interface MatchRuleMapTransform {

  Map<String, String> transform(Map<String, String> map);
}
