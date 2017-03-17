/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.http.HttpUtilities;
import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 * Match the form-urlencoded POST parameters in the request body
 * @author dvinegra
 */
public class MatchBodyPostParameters extends MatchBody {

  private static final Logger logger = Logger.getLogger("MatchBodyPostParameters");

  private final MatchRuleMapTransform _transform;

  public MatchBodyPostParameters() {
    this(null);
  }

  public MatchBodyPostParameters(MatchRuleMapTransform transform) {
    if (transform != null) {
      _transform = transform;
    } else {
      _transform = new MatchRuleIdentityTransform();
    }
  }

  @Override
  public boolean test(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    if (HttpUtilities.isFormURLEncodedContentType(incomingRequest.getContentType()) && HttpUtilities
        .isFormURLEncodedContentType(expectedRequest.getContentType())) {
      try {
        Map<String, String> incomingParams = getPostParametersFromRequest(incomingRequest);
        Map<String, String> expectedParams = getPostParametersFromRequest(expectedRequest);
        return testParameterEquivalency(incomingParams, expectedParams);
      } catch (UnsupportedEncodingException e) {
        logger.error("Caught exception " + e + " while decoding POST parameters");
      }
    }
    return super.test(incomingRequest, expectedRequest);
  }

  private Map<String, String> getPostParametersFromRequest(RecordedHttpRequest request)
      throws UnsupportedEncodingException {
    RecordedHttpBody body = request.getHttpBody();
    if (body instanceof RecordedEncodedHttpBody) {
      body = ((RecordedEncodedHttpBody) body).getDecodedBody();
    }
    assert (body instanceof RecordedStringHttpBody);
    String content = ((RecordedStringHttpBody) body).getContent();
    return HttpUtilities.stringToUrlParams(content, request.getCharset());
  }

  /**
   *  Tests whether the maps have the same key/value pairs in the same order
   */
  private boolean testParameterEquivalency(Map<String, String> incomingParams, Map<String, String> expectedParams) {
    return _transform.transform(incomingParams).toString().equals(_transform.transform(expectedParams).toString());
  }

  @Override
  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    StringBuilder resultBuilder = new StringBuilder("HTTP Body Parameters Mismatch");
    if (_transform instanceof MatchRuleBlacklistTransform) {
      resultBuilder.append(" (with Blacklist)");
    } else if (_transform instanceof MatchRuleWhitelistTransform) {
      resultBuilder.append(" (with Whitelist)");
    }
    try {
      Map<String, String> incomingParams = getPostParametersFromRequest(incomingRequest);
      Map<String, String> expectedParams = getPostParametersFromRequest(expectedRequest);
      resultBuilder.append("%n")
          .append(String.format("Incoming Parameters: %s%n", _transform.transform(incomingParams)))
          .append(String.format("Expected Parameters: %s%n", _transform.transform(expectedParams)));
    } catch (UnsupportedEncodingException e) {
      logger.error("Caught exception " + e + " while decoding POST parameters");
    }
    return resultBuilder.toString();
  }
}
