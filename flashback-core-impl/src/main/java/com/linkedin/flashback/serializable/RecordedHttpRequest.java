/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import java.net.URI;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Recorded Http request that contains uri and method.
 * @author shfeng
 */
public class RecordedHttpRequest extends RecordedHttpMessage {
  private String _httpMethod;
  private URI _uri;

  public RecordedHttpRequest(String httpMethod, URI uri, Map<String, String> headers,
      RecordedHttpBody recordedHttpBody) {
    super(headers, recordedHttpBody);
    _httpMethod = httpMethod;
    _uri = uri;
  }

  public String getMethod() {
    return _httpMethod;
  }

  public URI getUri() {
    return _uri;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
