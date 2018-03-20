package com.linkedin.flashback.matchrules;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.HttpHeaders;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author kagale
 */
public class MatchBodyWithAnyBoundaryTest {

  @Test
  public void testIsStringBodyMatch() {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("abc");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("abc");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testBothNullBodyMatch() {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, null);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNullAndEmptyBodyMatch() {
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testNullAndNonEmptyBodyNotMatch() {
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("abc");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, null);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testIsStringBodyNotMatch() {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("abc");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("abcd");
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, null, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, null, stringHttpBody2);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testIsStringBodyNotMatchWithDiffCharset() {
    RecordedStringHttpBody stringHttpBody1 = new RecordedStringHttpBody("造字");
    RecordedStringHttpBody stringHttpBody2 = new RecordedStringHttpBody("造字");
    Multimap<String, String> headers1 = LinkedHashMultimap.create();
    headers1.put(HttpHeaders.CONTENT_TYPE, "text/html; charset=euc-kr");
    Multimap<String, String> headers2 = LinkedHashMultimap.create();
    headers2.put(HttpHeaders.CONTENT_TYPE, "text/html; charset=big5");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, stringHttpBody2);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testBodyMatchForMultipartDataWithDifferentBoundary() {
    RecordedHttpBody stringHttpBody1 = new RecordedStringHttpBody("------wxyz1234abcd5e\nContent-Disposition: form-data; name=\"org\" \nMMM\n------wxyz1234abcd5e");
    RecordedHttpBody stringHttpBody2 = new RecordedStringHttpBody("------abcd5678wxyz4v\nContent-Disposition: form-data; name=\"org\" \nMMM\n------abcd5678wxyz4v");
    Multimap<String, String> headers1 = LinkedHashMultimap.create();
    headers1.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=wxyz1234abcd5e");
    Multimap<String, String> headers2 = LinkedHashMultimap.create();
    headers2.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=abcd5678wxyz4v");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, stringHttpBody1);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, stringHttpBody2);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testBodyMatchForMultipartDataWithSameBoundary() {
    RecordedHttpBody incomingHttpBody = new RecordedStringHttpBody("------wxyz1234abcd5e\nContent-Disposition: form-data; name=\"org\" \nMMM\n------wxyz1234abcd5e");
    RecordedHttpBody expectedHttpBody = new RecordedStringHttpBody("------wxyz1234abcd5e\nContent-Disposition: form-data; name=\"org\" \nMMM\n------wxyz1234abcd5e");
    Multimap<String, String> headers1 = LinkedHashMultimap.create();
    headers1.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=wxyz1234abcd5e");
    Multimap<String, String> headers2 = LinkedHashMultimap.create();
    headers2.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=wxyz1234abcd5e");

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest(null, null, headers1, incomingHttpBody);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest(null, null, headers2, expectedHttpBody);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertTrue(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }

  @Test
  public void testBodyMatchForDifferentRequestTypes() {
    RecordedHttpBody incomingHttpBody = new RecordedStringHttpBody("------wxyz1234abcd5e\nContent-Disposition: form-data; name=\"org\" \nMMM\n------wxyz1234abcd5e");
    Multimap<String, String> headers1 = LinkedHashMultimap.create();
    headers1.put(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=wxyz1234abcd5e");
    Multimap<String, String> headers2 = LinkedHashMultimap.create();

    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest("POST", null, headers1, incomingHttpBody);
    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest("GET", null, headers2, null);
    MatchRule matchRule = new MatchBodyWithAnyBoundary();
    Assert.assertFalse(matchRule.test(recordedHttpRequest1, recordedHttpRequest2));
  }
}
