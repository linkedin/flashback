/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.decorator.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;


/**
 * Compressor that compress bytes to gzip format
 *
 * @author shfeng
 */
public class GzipCompressor extends AbstractCompressor {
  @Override
  protected OutputStream getOutputStream(OutputStream output)
      throws IOException {
    return new GZIPOutputStream(output);
  }
}
