package com.linkedin.mitm.services;

import com.linkedin.mitm.model.RequestInfo;


/**
 * Is used to determine if a connection or a request is allowed by the proxy.
 */
public interface AccessService {
  /**
   *
   * Called when a connect request is issued so we only have info about the service name, destination and port.\
   *
   * @param serviceName
   * @param requestInfo
   * @return true or false depending on if the domain/port combination for the specified service has been whitelisted
   */
  boolean isValidConnection(String serviceName, RequestInfo requestInfo);

  /**
   * @param serviceName
   * @param requestInfo
   * @return true if any of the protocol rules defined in the config evaluates to true for the given request info.
   */
  boolean isValidRequest(String serviceName, RequestInfo requestInfo);
}
