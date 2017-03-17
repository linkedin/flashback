/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.builder;

import com.google.common.base.Strings;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import java.net.URI;
import java.net.URISyntaxException;


/**
 * Implementation of builder for {@link com.linkedin.flashback.serializable.RecordedHttpRequest}
 *
 * @author shfeng.
 */
public class RecordedHttpRequestBuilder extends RecordedHttpMessageBuilder {
  private String _httpMethod;
  private URI _uri;
  private transient String _path = "";

  /**
   * Take necessary available parameters from original netty http requests
   * If uri we get is not absolute, we need check if we can get it from host headers.
   * if not, we store relative path to _path.
   * */
  public RecordedHttpRequestBuilder(HttpRequest nettyHttpRequest) {
    super(nettyHttpRequest);
    interpretHttpRequest(nettyHttpRequest);
  }

  public void interpretHttpRequest(HttpRequest nettyHttpRequest) {
    _httpMethod = nettyHttpRequest.getMethod().toString();
    try {
      URI uri = new URI(nettyHttpRequest.getUri());
      if (uri.isAbsolute()) {
        _uri = uri;
      } else {
        String hostName = getHeader(HttpHeaders.Names.HOST);
        if (!Strings.isNullOrEmpty(hostName)) {
          _uri = new URI(String.format("https://%s%s", hostName, uri));
        } else {
          _path = uri.toString();
        }
      }
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Invalid URI in underlying request", e);
    }
  }

  /**
   * Add headers from http message and also check if uri is properly set.
   * If not, we need check host header and construct uri using relative path
   * and host name.
   *
   * @param httpMessage netty http message
   * */
  @Override
  public void addHeaders(HttpMessage httpMessage) {
    super.addHeaders(httpMessage);
    if (_uri == null) {
      String hostName = getHeader(HttpHeaders.Names.HOST);
      if (!Strings.isNullOrEmpty(hostName)) {
        try {
          _uri = new URI(String.format("https://%s%s", hostName, _path));
        } catch (URISyntaxException e) {
          throw new IllegalStateException("Invalid URI in underlying request", e);
        }
      }
    }
  }

  public RecordedHttpRequest build() {
    return new RecordedHttpRequest(_httpMethod, _uri, getHeaders(), getBody());
  }
}
