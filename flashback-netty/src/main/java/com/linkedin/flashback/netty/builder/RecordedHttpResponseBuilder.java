/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.netty.builder;

import com.linkedin.flashback.serializable.RecordedHttpResponse;
import io.netty.handler.codec.http.HttpResponse;


/**
 * Implementation of builder for {@link com.linkedin.flashback.serializable.RecordedHttpResponse}
 *
 * @author shfeng.
 */
public class RecordedHttpResponseBuilder extends RecordedHttpMessageBuilder {
  private int _status;

  public RecordedHttpResponseBuilder(HttpResponse nettyHttpResponse) {
    super(nettyHttpResponse);
    _status = nettyHttpResponse.getStatus().code();
  }

  public RecordedHttpResponse build() {
    return new RecordedHttpResponse(_status, getHeaders(), getBody());
  }
}
