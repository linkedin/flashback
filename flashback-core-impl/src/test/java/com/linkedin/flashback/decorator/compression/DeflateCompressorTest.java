/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.decorator.compression;

import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 * @author dvinegra
 */
public class DeflateCompressorTest {
  @Test
  public void testCompress()
      throws IOException {
    String str = "Hello world";
    String charset = "ISO-8859-1";
    byte[] content = str.getBytes(charset);
    DeflateCompressor deflateCompressor = new DeflateCompressor();

    byte[] compressedContent = deflateCompressor.compress(content);
    Assert.assertNotEquals(deflateCompressor.compress(content).length, content.length);

    DeflateDecompressor deflateDecompressor = new DeflateDecompressor();
    Assert.assertEquals(deflateDecompressor.decompress(compressedContent), content);
  }
}
