/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.decorator.compression;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Abstract compressor class
 *
 * @author shfeng
 */
public abstract class AbstractCompressor {

  public byte[] compress(byte[] encodedBytes)
      throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (OutputStream stream = getOutputStream(out)) {
      stream.write(encodedBytes);
      stream.flush();
    }
    return out.toByteArray();
  }

  abstract protected OutputStream getOutputStream(OutputStream output)
      throws IOException;
}
