package com.linkedin.mitm.model;

import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Holds information about the http request such as path and method.
 */
public class HttpRequestInfo extends RequestInfo {
  private String _path;
  private HttpMethod _method;

  public HttpRequestInfo(RequestInfo requestInfo) {
    super(requestInfo.getDomain(), requestInfo.getPort());
  }

  public String getPath() {
    return _path;
  }

  public HttpRequestInfo setPath(String path) {
    _path = path;
    return this;
  }

  public HttpMethod getMethod() {
    return _method;
  }

  public HttpRequestInfo setMethod(HttpMethod method) {
    _method = method;
    return this;
  }

  @Override
  public Protocol getProtocol() {
    return Protocol.HTTP;
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
    return "path=" + _path + ", method=" + _method + ", " + super.toString();
  }
}
