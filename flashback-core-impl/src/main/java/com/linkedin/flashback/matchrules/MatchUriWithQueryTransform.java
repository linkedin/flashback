/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.http.HttpUtilities;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.log4j.Logger;


/**
 * Match the URI, modifying the query parameters as specified by the transform
 * This only applies to query parameters of the top-level URI
 * @author dvinegra
 */
public class MatchUriWithQueryTransform extends MatchUri {

  private static final Logger LOGGER = Logger.getLogger("MatchUriWithQueryTransform");

  private final MatchRuleMapTransform _transform;

  public MatchUriWithQueryTransform(MatchRuleMapTransform transform) {
    if (transform != null) {
      _transform = transform;
    } else {
      _transform = new MatchRuleIdentityTransform();
    }
  }

  @Override
  protected boolean testUriEquivalency(URI incomingUri, URI expectedUri) {
    try {
      URI modifiedIncomingUri = getModifiedUri(incomingUri);
      URI modifiedExpectedUri = getModifiedUri(expectedUri);
      return super.testUriEquivalency(modifiedIncomingUri, modifiedExpectedUri);
    } catch (URISyntaxException | UnsupportedEncodingException e) {
      LOGGER.error("Caught exception " + e + " while constructing modified URI");
    }
    return false;
  }

  @Override
  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    StringBuilder resultBuilder = new StringBuilder("URI Mismatch");
    if (_transform instanceof MatchRuleBlacklistTransform) {
      resultBuilder.append(" (with Query Blacklist)");
    } else if (_transform instanceof MatchRuleWhitelistTransform) {
      resultBuilder.append(" (with Query Whitelist)");
    }
    try {
      URI modifiedIncomingUri = getModifiedUri(incomingRequest.getUri());
      URI modifiedExpectedUri = getModifiedUri(expectedRequest.getUri());
      resultBuilder.append("%n")
          .append(String.format("Incoming URI: %s%n", modifiedIncomingUri))
          .append(String.format("Expected URI: %s%n", modifiedExpectedUri));
    } catch (URISyntaxException | UnsupportedEncodingException e) {
      LOGGER.error("Caught exception " + e + " while constructing modified URI");
    }
    return resultBuilder.toString();
  }

  private URI getModifiedUri(URI uri)
      throws UnsupportedEncodingException, URISyntaxException {
    Map<String, String> incomingParams = HttpUtilities.stringToUrlParams(uri.getRawQuery(), HttpUtilities.UTF8_CONSTANT);
    String modifiedIncomingQuery = HttpUtilities.urlParamsToString(_transform.transform(incomingParams), HttpUtilities.UTF8_CONSTANT);
    return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), modifiedIncomingQuery, uri.getFragment());
  }
}
