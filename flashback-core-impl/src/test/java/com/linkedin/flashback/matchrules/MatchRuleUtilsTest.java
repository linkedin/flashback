/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author dvinegra
 */
public class MatchRuleUtilsTest {

  @Test
  public void testEntireRequestMatch()
      throws Exception {

    Map<String, String> headers = new HashMap<>();
    headers.put("key1", "value1");
    headers.put("key2", "value2");

    RecordedHttpBody body = new RecordedStringHttpBody("body");
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("POST", new URI("http://www.google.com/"), headers, body);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("POST", new URI("http://www.google.com/"), headers, body);

    MatchRule entireRequestRule = MatchRuleUtils.matchEntireRequest();
    MatchRule methodUriRule = MatchRuleUtils.matchMethodUri();

    Assert.assertTrue(entireRequestRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(methodUriRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testEntireRequestMatchNoBody()
      throws Exception {

    Map<String, String> headers = new HashMap<>();
    headers.put("key1", "value1");
    headers.put("key2", "value2");

    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("POST", new URI("http://www.google.com/"), headers, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("POST", new URI("http://www.google.com/"), headers, null);

    MatchRule entireRequestRule = MatchRuleUtils.matchEntireRequest();
    MatchRule methodUriRule = MatchRuleUtils.matchMethodUri();

    Assert.assertTrue(entireRequestRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(methodUriRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testDifferentMethods()
      throws Exception {

    Map<String, String> headers = new HashMap<>();
    headers.put("key1", "value1");
    headers.put("key2", "value2");

    RecordedHttpBody body = new RecordedStringHttpBody("body");
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("POST", new URI("http://www.google.com/"), headers, body);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("PUT", new URI("http://www.google.com/"), headers, body);

    MatchRule entireRequestRule = MatchRuleUtils.matchEntireRequest();
    MatchRule methodUriRule = MatchRuleUtils.matchMethodUri();

    Assert.assertFalse(entireRequestRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertFalse(methodUriRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(entireRequestRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Method Mismatch"));
    Assert.assertTrue(methodUriRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Method Mismatch"));
  }

  @Test
  public void testDifferentHeaders()
      throws Exception {

    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key3", "value3");

    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("GET", new URI("http://www.google.com/"), headers1, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("GET", new URI("http://www.google.com/"), headers2, null);

    MatchRule entireRequestRule = MatchRuleUtils.matchEntireRequest();
    MatchRule methodUriRule = MatchRuleUtils.matchMethodUri();

    Assert.assertFalse(entireRequestRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(methodUriRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(entireRequestRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Headers Mismatch"));
  }

  @Test
  public void testDifferentURIs()
      throws Exception {

    Map<String, String> headers = new HashMap<>();
    headers.put("key1", "value1");
    headers.put("key2", "value2");

    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("GET", new URI("http://www.google.com/"), headers, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("GET", new URI("http://www.google.com/foo/"), headers, null);

    MatchRule entireRequestRule = MatchRuleUtils.matchEntireRequest();
    MatchRule methodUriRule = MatchRuleUtils.matchMethodUri();

    Assert.assertFalse(entireRequestRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertFalse(methodUriRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(entireRequestRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("URI Mismatch"));
    Assert.assertTrue(entireRequestRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("URI Mismatch"));
  }

  @Test
  public void testDifferentBody()
      throws Exception {

    Map<String, String> headers = new HashMap<>();
    headers.put("key1", "value1");
    headers.put("key2", "value2");

    RecordedHttpBody body1 = new RecordedStringHttpBody("body1");
    RecordedHttpBody body2 = new RecordedStringHttpBody("body2");
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("POST", new URI("http://www.google.com/"), headers, body1);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("POST", new URI("http://www.google.com/"), headers, body2);

    MatchRule entireRequestRule = MatchRuleUtils.matchEntireRequest();
    MatchRule methodUriRule = MatchRuleUtils.matchMethodUri();

    Assert.assertFalse(entireRequestRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(methodUriRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(entireRequestRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Body Mismatch"));
  }

  @Test
  public void testHeaderWhitelistMatch()
      throws Exception {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key2", "value2");
    headers2.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);

    Set<String> wList = new HashSet<>();
    wList.add("key1");
    wList.add("key2");

    MatchRule matchRule = MatchRuleUtils.matchHeadersWithWhitelist(wList);

    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testHeaderWhitelistMatchWithNoneInWL()
      throws Exception {
    Map<String, String> headers1 = new HashMap();
    headers1.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key2", "value2");
    headers2.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);

    Set<String> wList = new HashSet<>();
    wList.add("key1");

    MatchRule matchRule = MatchRuleUtils.matchHeadersWithWhitelist(wList);

    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testHeaderWhitelistNotMatchWithOneHeaderNull()
      throws Exception {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key2", "value2");
    headers2.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);

    Set<String> wList = new HashSet<>();
    wList.add("key1");

    MatchRule matchRule = MatchRuleUtils.matchHeadersWithWhitelist(wList);

    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Headers Mismatch (with Whitelist)"));
  }

  @Test
  public void testHeaderBlacklistMatch()
      throws Exception {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key2", "value2");
    headers1.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key2", "value2");
    headers2.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);

    Set<String> blList = new HashSet<>();
    blList.add("key1");

    MatchRule matchRule = MatchRuleUtils.matchHeadersWithBlacklist(blList);

    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testHeaderBlacklistMatchEmptyBL()
      throws Exception {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);

    Set<String> blList = new HashSet<>();
    MatchRule matchRule = MatchRuleUtils.matchHeadersWithBlacklist(blList);

    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testHeaderBlacklistMatchNullBL() {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);

    MatchRule matchRule = MatchRuleUtils.matchHeadersWithBlacklist(null);

    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testHeaderBlacklistNotMatchBL()
      throws Exception {
    Map<String, String> headers1 = new HashMap<>();
    headers1.put("key1", "value1");
    headers1.put("key2", "value2");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, null);

    Map<String, String> headers2 = new HashMap<>();
    headers2.put("key1", "value1");
    headers2.put("key2", "value2");
    headers2.put("key3", "value3");
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, null);

    Set<String> blList = new HashSet<>();
    blList.add("key1");
    MatchRule matchRule = MatchRuleUtils.matchHeadersWithBlacklist(blList);

    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Headers Mismatch (with Blacklist)"));
  }

  @Test
  public void testPostParameterWhitelistMatch()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=x&c=c&d=d");

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    Set<String> whitelist = new HashSet<>();
    whitelist.add("a");
    whitelist.add("c");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = MatchRuleUtils.matchBodyPostParametersWithWhitelist(whitelist);
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testPostParameterWhitelistMatchWithNullParameter()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c&e=");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=x&c=c&d=d&e=");

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    Set<String> whitelist = new HashSet<>();
    whitelist.add("a");
    whitelist.add("c");
    whitelist.add("e");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = MatchRuleUtils.matchBodyPostParametersWithWhitelist(whitelist);
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testPostParameterWhitelistNotMatch()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=x&c=c");

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    Set<String> whitelist = new HashSet<>();
    whitelist.add("a");
    whitelist.add("b");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = MatchRuleUtils.matchBodyPostParametersWithWhitelist(whitelist);
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Body Parameters Mismatch (with Whitelist)"));
  }

  @Test
  public void testPostParameterBlacklistMatch()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=x&c=c&d=d");

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    Set<String> blackList = new HashSet<>();
    blackList.add("b");
    blackList.add("d");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = MatchRuleUtils.matchBodyPostParametersWithBlacklist(blackList);
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testPostParameterBlacklistNotMatch()
      throws Exception {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("a=a&b=b&c=c");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("a=a&b=x&c=c");

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/x-www-form-urlencoded");

    Set<String> blackList = new HashSet<>();
    blackList.add("c");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers, stringHttpBody2);
    MatchRule matchRule = MatchRuleUtils.matchBodyPostParametersWithBlacklist(blackList);
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("HTTP Body Parameters Mismatch (with Blacklist)"));
  }

  @Test
  public void testUriWhitelistExactMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a"), null, null);
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryWhitelist(new HashSet<>());
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testUriWhitelistMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=b&c=c"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=z&c=c"), null, null);
    HashSet<String> whitelist = new HashSet<>();
    whitelist.add("c");
    whitelist.add("d");
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryWhitelist(whitelist);
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testUriWhitelistNotMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=b&c=c"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=z&c=c"), null, null);
    HashSet<String> whitelist = new HashSet<>();
    whitelist.add("b");
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryWhitelist(whitelist);
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("URI Mismatch (with Query Whitelist)"));
  }

  @Test
  public void testUriWhitelistNotMatchDifferentOrder()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?b=b&c=c&a=a"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?b=z&a=a&c=c"), null, null);
    HashSet<String> whitelist = new HashSet<>();
    whitelist.add("a");
    whitelist.add("c");
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryWhitelist(whitelist);
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("URI Mismatch (with Query Whitelist)"));
  }

  @Test
  public void testUriBlacklistExactMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a"), null, null);
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryBlacklist(new HashSet<>());
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testUriBlacklistMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=b&c=c"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=z&c=c"), null, null);
    HashSet<String> blacklist = new HashSet<>();
    blacklist.add("b");
    blacklist.add("c");
    blacklist.add("d");
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryBlacklist(blacklist);
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testUriBlacklistMatchWithNullParameter()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=b&c=c&e="), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=z&c=c&e="), null, null);
    HashSet<String> blacklist = new HashSet<>();
    blacklist.add("b");
    blacklist.add("c");
    blacklist.add("d");
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryBlacklist(blacklist);
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testUriBlacklistNotMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=b&c=c"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?a=a&b=z&c=c"), null, null);
    HashSet<String> blacklist = new HashSet<>();
    blacklist.add("c");
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryBlacklist(blacklist);
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("URI Mismatch (with Query Blacklist)"));
  }

  @Test
  public void testUriBlacklistNotMatchDifferentOrder()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?b=b&c=c&a=a"), null, null);
    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest(null, new URI("http://www.google.com/?b=z&a=a&c=c"), null, null);
    HashSet<String> blacklist = new HashSet<>();
    blacklist.add("b");
    MatchRule matchRule = MatchRuleUtils.matchUriWithQueryBlacklist(blacklist);
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
    Assert.assertTrue(matchRule.getMatchFailureDescriptionForRequests(recordedHttpRequest1, recordedHttpRequest2).contains("URI Mismatch (with Query Blacklist)"));
  }
}
