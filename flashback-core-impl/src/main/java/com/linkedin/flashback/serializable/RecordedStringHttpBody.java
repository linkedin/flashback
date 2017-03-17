/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Recorded http body that obtains its content from a String
 * @author shfeng
 */
public class RecordedStringHttpBody implements RecordedHttpBody {
  private final String _content;

  public RecordedStringHttpBody(String content) {
    _content = content;
  }

  @Override
  public byte[] getContent(String charSet) {
    try {
      return _content.getBytes(charSet);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to get bytes from string");
    }
  }

  public String getContent() {
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
