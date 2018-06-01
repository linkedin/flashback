package com.linkedin.mitm.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Holds the domain and port of a request. Can be extended to hold other properties off requests once the protocol is known.
 */
public class RequestInfo {
  private final String _domain;
  private final int _port;

  public RequestInfo(String domain, int port) {
    _domain = domain;
    _port = port;
  }

  public String getDomain() {
    return _domain;
  }

  public int getPort() {
    return _port;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public String toString() {
    return "port=" + _port + ", domain=" + _domain;
  }

  // can be overriden when extending
  public Protocol getProtocol() {
    return Protocol.BINARY;
  }
}