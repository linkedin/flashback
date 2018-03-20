package com.linkedin.flashback.matchrules;

import com.google.common.net.HttpHeaders;
import com.linkedin.flashback.serializable.RecordedByteHttpBody;
import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.io.IOException;
import java.util.Arrays;


/**
 * Match rule to match two RecordedHttpBody where boundary values differ
 * but all other content remains same.
 *
 * @author kagale
 */
public class MatchBodyWithAnyBoundary extends BaseMatchRule {

  private static final String BOUNDARY = "boundary=";
  private static final String MULTIPART = "multipart";
  private static final String EMPTY = "";
  private static final String SEMI_COLON = ";";

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
      // Go inside only for requests with multipart data.
      if (incomingRequest.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE) && expectedRequest.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)
          && incomingRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE).iterator().next().contains(MULTIPART)
          && expectedRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE).iterator().next().contains(MULTIPART)) {
        // Get boundary values.
        String currentRequestBoundaryValue = Arrays.stream(incomingRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE).iterator().next()
            .split(SEMI_COLON))
            .filter((attr) -> attr.contains(BOUNDARY))
            .findFirst()
            .orElse(EMPTY)
            .replaceFirst(BOUNDARY, EMPTY)
            .trim();
        String recordedRequestBoundaryValue = Arrays.stream(expectedRequest.getHeaders().get(HttpHeaders.CONTENT_TYPE).iterator().next()
            .split(SEMI_COLON))
            .filter((attr) -> attr.contains(BOUNDARY))
            .findFirst()
            .orElse(EMPTY)
            .replaceFirst(BOUNDARY, EMPTY)
            .trim();

        // Prepare request for matching
        // i.e., by overriding the boundary value
        if (!currentRequestBoundaryValue.equals(recordedRequestBoundaryValue)) {
          // Override the boundary value of request body.
          String recordedBody = new String(expectedBody.getContent(charSet2)).replaceAll(recordedRequestBoundaryValue,
              currentRequestBoundaryValue);
          expectedBody = new RecordedByteHttpBody(recordedBody.getBytes(charSet2));
        }
      }
      return Arrays.equals(incomingBody.getContent(charSet1), expectedBody.getContent(charSet2));
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert to byte arrays", e);
    }
  }

  @Override
  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    RecordedHttpBody incomingBody = getBodyFromRequest(incomingRequest);
    RecordedHttpBody expectedBody = getBodyFromRequest(expectedRequest);
    if (incomingBody instanceof RecordedStringHttpBody && expectedBody instanceof RecordedStringHttpBody) {
      return String.format("HTTP Body Mismatch\nIncoming Body: %s\nExpected Body: %s\n",
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
