/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.serializable.RecordedByteHttpBody;
import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.io.IOException;
import java.util.Arrays;


/**
 * Match rule to match two RecordedHttpBody
 * @author shfeng
 */
public class MatchBody extends BaseMatchRule {
  @Override
  public boolean test(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    String charSet1 = incomingRequest.getCharset();
    String charSet2 = expectedRequest.getCharset();
    RecordedHttpBody incomingBody = incomingRequest.getHttpBody();
    if (incomingBody == null) {
      incomingBody = new RecordedByteHttpBody(new byte[0]);
    }
    RecordedHttpBody expectedBody = expectedRequest.getHttpBody();
    if (expectedBody == null) {
      expectedBody = new RecordedByteHttpBody(new byte[0]);
    }

    try {
      return Arrays.equals(incomingBody.getContent(charSet1), expectedBody.getContent(charSet2));
    } catch (IOException e) {
      //TODO: PLACEHOLDER, error handling will be in separate RB.
      throw new RuntimeException("Failed to convert to byte arrays", e);
    }
  }

  @Override
  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    RecordedHttpBody incomingBody = getBodyFromRequest(incomingRequest);
    RecordedHttpBody expectedBody = getBodyFromRequest(expectedRequest);
    if (incomingBody instanceof RecordedStringHttpBody && expectedBody instanceof RecordedStringHttpBody) {
      return String.format("HTTP Body Mismatch%nIncoming Body: %s%nExpected Body: %s%n",
          ((RecordedStringHttpBody) incomingBody).getContent(),
          ((RecordedStringHttpBody) expectedBody).getContent());
    } else {
      return "HTTP Body Mismatch (binary bodies differ)";
    }
  }

  private RecordedHttpBody getBodyFromRequest(RecordedHttpRequest request) {
    RecordedHttpBody body = request.getHttpBody();
    if (body instanceof RecordedEncodedHttpBody) {
      return ((RecordedEncodedHttpBody) body).getDecodedBody();
    } else {
      return body;
    }
  }
}
