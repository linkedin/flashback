/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.factory;

import com.linkedin.flashback.decorator.compression.DeflateCompressor;
import com.linkedin.flashback.decorator.compression.GzipCompressor;
import com.linkedin.flashback.serializable.RecordedByteHttpBody;
import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng .
 * @author dvinegra
 */
public class RecordedHttpBodyFactoryTest {
  @Test
  public void testCreateStringHttpBody()
      throws IOException {
    String str = "Hello world";
    byte[] content = str.getBytes();
    int size = content.length;
    InputStream is = new ByteArrayInputStream(content);
    RecordedHttpBody recordedHttpBody = RecordedHttpBodyFactory.create("text/html", "identity", is, "UTF-8");
    Assert.assertTrue(recordedHttpBody instanceof RecordedStringHttpBody);
    Assert.assertFalse(recordedHttpBody instanceof RecordedByteHttpBody);
    Assert.assertEquals(size, recordedHttpBody.getContent("UTF-8").length);
  }

  @Test
  public void testCreateByteHttpBody()
      throws IOException {
    String str = "Hello world";
    byte[] content = str.getBytes();
    InputStream is = new ByteArrayInputStream(content);
    RecordedHttpBody recordedHttpBody = RecordedHttpBodyFactory.create("image/gif", "identity", is, "UTF-8");
    Assert.assertTrue(recordedHttpBody instanceof RecordedByteHttpBody);
    Assert.assertFalse(recordedHttpBody instanceof RecordedStringHttpBody);
    Assert.assertEquals(content, recordedHttpBody.getContent("UTF-8"));
  }

  @Test
  public void testCreateGZipEncodedStringHttpBody()
      throws IOException {
    String str = "Hello world. This is some extra text to make the string longer so that gzip makes it smaller";
    byte[] content = str.getBytes();
    byte[] compressedContent = new GzipCompressor().compress(content);
    InputStream is = new ByteArrayInputStream(compressedContent);
    RecordedHttpBody recordedHttpBody = RecordedHttpBodyFactory.create("text/html", "gzip", is, "UTF-8");
    Assert.assertTrue(recordedHttpBody instanceof RecordedEncodedHttpBody);
    RecordedEncodedHttpBody encodedBody = (RecordedEncodedHttpBody) recordedHttpBody;
    Assert.assertEquals(encodedBody.getEncodingName(), "gzip");
    Assert.assertTrue(encodedBody.getDecodedBody() instanceof RecordedStringHttpBody);
    Assert.assertEquals(encodedBody.getDecodedBody().getContent("UTF-8"), content);
    Assert.assertEquals(encodedBody.getContent("UTF-8"), compressedContent);
  }

  @Test
  public void testCreateDeflateEncodedStringHttpBody()
      throws IOException {
    String str = "Hello world. This is some extra text to make the string longer so that deflate makes it smaller";
    byte[] content = str.getBytes();
    byte[] compressedContent = new DeflateCompressor().compress(content);
    InputStream is = new ByteArrayInputStream(compressedContent);
    RecordedHttpBody recordedHttpBody = RecordedHttpBodyFactory.create("text/html", "deflate", is, "UTF-8");
    Assert.assertTrue(recordedHttpBody instanceof RecordedEncodedHttpBody);
    RecordedEncodedHttpBody encodedBody = (RecordedEncodedHttpBody) recordedHttpBody;
    Assert.assertEquals(encodedBody.getEncodingName(), "deflate");
    Assert.assertTrue(encodedBody.getDecodedBody() instanceof RecordedStringHttpBody);
    Assert.assertEquals(encodedBody.getDecodedBody().getContent("UTF-8"), content);
    Assert.assertEquals(encodedBody.getContent("UTF-8"), compressedContent);
  }
}
