/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import com.linkedin.flashback.http.HttpUtilities;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.log4j.Logger;


/**
 * Match rule to match Http uri
 * @author shfeng
 */
public class MatchUri extends BaseMatchRule {

  private static final String MODULE = MatchUri.class.getName();
  private static final Logger LOGGER = Logger.getLogger(MODULE);

  @Override
  public boolean test(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    return testUriEquivalency(incomingRequest.getUri(), expectedRequest.getUri());
  }

  @Override
  public String getMatchFailureDescriptionForRequests(RecordedHttpRequest incomingRequest, RecordedHttpRequest expectedRequest) {
    return String.format("URI Mismatch%nIncoming URI: %s%nExpected URI: %s%n",
        incomingRequest.getUri(),
        expectedRequest.getUri());
  }

  protected boolean testUriEquivalency(URI incomingUri, URI expectedUri) {
    try {
      URI incomingCanonicalizedUri = getCanonicalizedUri(incomingUri);
      URI expectedCanonicalizedUri = getCanonicalizedUri(expectedUri);
      return incomingCanonicalizedUri.equals(expectedCanonicalizedUri);
    } catch (URISyntaxException e) {
      LOGGER.error("Caught exception " + e + " while constructing modified URI");
    }
    return incomingUri.equals(expectedUri);
  }

  private int getPortForScheme(String scheme) {
    if (HttpUtilities.HTTP_SCHEME.equalsIgnoreCase(scheme)) {
      return HttpUtilities.HTTP_DEFAULT_PORT;
    } else if (HttpUtilities.HTTPS_SCHEME.equalsIgnoreCase(scheme)) {
      return HttpUtilities.HTTPS_DEFAULT_PORT;
    }
    return -1;
  }

  /**
   * Returns a canonicalized version of the Uri, accounting for the default port for the scheme
   * @param uri
   * @return a canonicalized URI with the default port for the scheme (http or https) explicitly added if no port is specified
   * @throws java.net.URISyntaxException
   */
  private URI getCanonicalizedUri(URI uri)
      throws URISyntaxException {
    int defaultPort = getPortForScheme(uri.getScheme());
    if (uri.getPort() == -1 && defaultPort != -1) {
      return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), defaultPort, uri.getPath(), uri.getQuery(), uri.getFragment());
    }
    return uri;
  }
}
