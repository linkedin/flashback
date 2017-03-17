/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.decorator.compression;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;


/**
 * Decompressor that decompresses bytes in gzip format
 *
 * @author dvinegra
 */
public class GzipDecompressor extends AbstractDecompressor {
  @Override
  protected InputStream getInputStream(InputStream input)
      throws IOException {
    return new GZIPInputStream(input);
  }
}
