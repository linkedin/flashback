/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.decorator.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Abstract decompressor class
 *
 * @author dvinegra
 */
public abstract class AbstractDecompressor {

  public byte[] decompress(byte[] encodedBytes)
      throws IOException {
    byte[] decompressedBytes;
    try (InputStream stream = getInputStream(new ByteArrayInputStream(encodedBytes));
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      int bytesRead;
      byte[] decodedData = new byte[1024];
      while ((bytesRead = stream.read(decodedData)) != -1) {
        out.write(decodedData, 0, bytesRead);
      }
      out.flush();
      decompressedBytes = out.toByteArray();
    }
    return decompressedBytes;
  }

  abstract protected InputStream getInputStream(InputStream input)
      throws IOException;
}
