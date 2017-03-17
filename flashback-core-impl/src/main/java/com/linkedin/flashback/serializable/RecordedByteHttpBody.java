/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Recorded http body that obtains its content from a byte array
 * @author shfeng
 */
public class RecordedByteHttpBody implements RecordedHttpBody {
  private final byte[] _content;

  /**
   * Construct object with byte array
   * */
  public RecordedByteHttpBody(byte[] content) {
    _content = content;
  }

  @Override
  public byte[] getContent(String charSet) {
    return getContent();
  }

  public byte[] getContent() {
    return _content;
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
