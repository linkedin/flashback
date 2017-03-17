/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import java.util.Date;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Http exchange which contains the whole http interaction (request, response and time)
 * @author shfeng
 */
public class RecordedHttpExchange {
  private RecordedHttpRequest _recordedHttpRequest;
  private RecordedHttpResponse _recordedHttpResponse;
  private Date _updateTime;

  public RecordedHttpExchange(RecordedHttpRequest recordedHttpRequest, RecordedHttpResponse recordedHttpResponse,
      Date updateTime) {
    _recordedHttpRequest = recordedHttpRequest;
    _recordedHttpResponse = recordedHttpResponse;
    _updateTime = updateTime;
  }

  public RecordedHttpRequest getRecordedHttpRequest() {
    return _recordedHttpRequest;
  }

  public RecordedHttpResponse getRecordedHttpResponse() {
    return _recordedHttpResponse;
  }

  public Date getUpdateTime() {
    return _updateTime;
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
