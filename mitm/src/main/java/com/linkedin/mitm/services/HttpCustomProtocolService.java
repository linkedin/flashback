package com.linkedin.mitm.services;

import io.netty.handler.codec.http.HttpRequest;


/**
 * This service provides an opportunity for the user to handle any customized HTTP protocol that they use. Most importantly,
 * we want to allow the user to have some kind of authentication mechanism for HTTP requests other than TLS.
 * For example, a client may choose BEARER auth token as an authentication mechanism.
 */
public interface HttpCustomProtocolService {

  /**
   * Check if the incoming request is allowed to proceed or not.
   * @param request
   * @return true if the request should proceed further in the proxy flow.
   */
  default boolean isAllowed(HttpRequest request) {
    return true;
  }

}
