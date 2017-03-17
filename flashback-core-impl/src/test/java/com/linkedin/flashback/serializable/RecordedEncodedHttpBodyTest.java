/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import com.linkedin.flashback.decorator.compression.GzipCompressor;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author dvinegra
 */
public class RecordedEncodedHttpBodyTest {

  @Test
  public void testCreateFromDecodedStringBody()
      throws Exception {
    String str = "Gaap is awesome";
    byte[] content = str.getBytes();
    byte[] compressedContent = new GzipCompressor().compress(content);
    RecordedStringHttpBody recordedStringHttpBody = new RecordedStringHttpBody(str);
    RecordedEncodedHttpBody recordedEncodedHttpBody = new RecordedEncodedHttpBody(recordedStringHttpBody, "gzip");
    Assert.assertEquals(recordedEncodedHttpBody.getContent("UTF-8"), compressedContent);
  }

  @Test
  public void testCreateFromEncodedBytes()
      throws Exception {
    String str = "Gaap is awesome";
    byte[] content = str.getBytes();
    byte[] compressedContent = new GzipCompressor().compress(content);
    RecordedEncodedHttpBody recordedEncodedHttpBody =
        new RecordedEncodedHttpBody(compressedContent, "gzip", "UTF-8", "text/html");
    Assert.assertEquals(recordedEncodedHttpBody.getContent("UTF-8"), compressedContent);
    Assert.assertEquals(recordedEncodedHttpBody.getEncodingName(), "gzip");
    RecordedHttpBody decodedBody = recordedEncodedHttpBody.getDecodedBody();
    Assert.assertTrue(decodedBody instanceof RecordedStringHttpBody);
    Assert.assertEquals(((RecordedStringHttpBody) decodedBody).getContent(), str);
  }
}
