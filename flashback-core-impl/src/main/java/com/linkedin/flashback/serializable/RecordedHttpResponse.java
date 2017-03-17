/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Recorded Http response that contains status code.
 * @author shfeng
 */
public class RecordedHttpResponse extends RecordedHttpMessage {
  private int _status;

  public RecordedHttpResponse(int status, Map<String, String> headers, RecordedHttpBody recordedHttpBody) {
    super(headers, recordedHttpBody);
    _status = status;
  }

  public int getStatus() {
    return _status;
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
