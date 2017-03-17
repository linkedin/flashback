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
public class GzipCompressorTest {

  @Test
  public void testCompress()
      throws IOException {
    String str = "Hello world";
    String charset = "ISO-8859-1";
    byte[] content = str.getBytes(charset);
    GzipCompressor gzipCompressor = new GzipCompressor();

    byte[] compressedContent = gzipCompressor.compress(content);
    Assert.assertNotEquals(compressedContent.length, content.length);

    GzipDecompressor gzipDecompressor = new GzipDecompressor();
    Assert.assertEquals(gzipDecompressor.decompress(compressedContent), content);
  }
}
