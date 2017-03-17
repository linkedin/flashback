/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import java.util.Set;


/**
 * Class that provides convenience methods to create match rules based on common combinations
 * @author dvinegra
 */
public final class MatchRuleUtils {

  private MatchRuleUtils() {
  }

  /**
   * @return rule to match request URI, Method, Headers, and Body
   */
  public static MatchRule matchEntireRequest() {
    CompositeMatchRule compositeMatchRule = new CompositeMatchRule();
    compositeMatchRule.addRule(new MatchMethod());
    compositeMatchRule.addRule(new MatchUri());
    compositeMatchRule.addRule(new MatchHeaders());
    compositeMatchRule.addRule(new MatchBody());
    return compositeMatchRule;
  }

  /**
   * @return rule to match request URI and Method
   */
  public static MatchRule matchMethodUri() {
    CompositeMatchRule compositeMatchRule = new CompositeMatchRule();
    compositeMatchRule.addRule(new MatchMethod());
    compositeMatchRule.addRule(new MatchUri());
    return compositeMatchRule;
  }

  /**
   * @return rule to match request URI, Body and Method
   */
  public static MatchRule matchMethodBodyUri() {
    CompositeMatchRule compositeMatchRule = new CompositeMatchRule();
    compositeMatchRule.addRule(new MatchBody());
    compositeMatchRule.addRule(new MatchMethod());
    compositeMatchRule.addRule(new MatchUri());
    return compositeMatchRule;
  }

  /**
   * @return rule to match request URI, whitelisting the specified query parameters
   */
  public static MatchRule matchUriWithQueryWhitelist(Set<String> whiteList) {
    return new MatchUriWithQueryTransform(new MatchRuleWhitelistTransform(whiteList));
  }

  /**
   * @return rule to match request URI, blacklisting the specified query parameters
   */
  public static MatchRule matchUriWithQueryBlacklist(Set<String> blackList) {
    return new MatchUriWithQueryTransform(new MatchRuleBlacklistTransform(blackList));
  }

  /**
   * @return rule to match the request headers contained in the whitelist
   */
  public static MatchRule matchHeadersWithWhitelist(Set<String> whiteList) {
    return new MatchHeaders(new MatchRuleWhitelistTransform(whiteList));
  }

  /**
   * @return rule to match the request headers, except for those in the blacklist
   */
  public static MatchRule matchHeadersWithBlacklist(Set<String> blackList) {
    return new MatchHeaders(new MatchRuleBlacklistTransform(blackList));
  }

  /**
   * @return rule to match the request POST body parameters contained in the whitelist
   */
  public static MatchRule matchBodyPostParametersWithWhitelist(Set<String> whiteList) {
    return new MatchBodyPostParameters(new MatchRuleWhitelistTransform(whiteList));
  }

  /**
   * @return rule to match the request POST body parameters, except for those in the blacklist
   */
  public static MatchRule matchBodyPostParametersWithBlacklist(Set<String> blackList) {
    return new MatchBodyPostParameters(new MatchRuleBlacklistTransform(blackList));
  }
}
