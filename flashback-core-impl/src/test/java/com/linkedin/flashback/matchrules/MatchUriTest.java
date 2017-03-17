/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedHttpRequest;
import java.net.URI;
import java.net.URISyntaxException;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class MatchUriTest {
  @Test
  public void testIsMatch()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, new URI("google.com"), null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, new URI("google.com"), null, null);
    MatchRule matchRule = new MatchUri();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testMatchDefaultPortHttp()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, new URI("http://www.example.org/"), null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, new URI("http://www.example.org:80/"), null, null);
    MatchRule matchRule = new MatchUri();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testMatchDefaultPortHttps()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, new URI("https://www.example.org/"), null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, new URI("https://www.example.org:443/"), null, null);
    MatchRule matchRule = new MatchUri();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNotMatchNonDefaultPort()
      throws Exception {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, new URI("http://www.example.org/"), null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, new URI("http://www.example.org:8080/"), null, null);
    MatchRule matchRule = new MatchUri();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testIsNotMatch()
      throws URISyntaxException {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, new URI("google.com"), null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, new URI("yahoo.com"), null, null);
    MatchRule matchRule = new MatchUri();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }
}
