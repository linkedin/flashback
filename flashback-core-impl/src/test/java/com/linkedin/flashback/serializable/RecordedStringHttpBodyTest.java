/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class RecordedStringHttpBodyTest {
  @Test
  public void testGetByteArray()
      throws Exception {
    String str = "Hello world";
    byte[] content = str.getBytes();
    RecordedStringHttpBody recordedStringHttpBody = new RecordedStringHttpBody(str);
    Assert.assertEquals(content, recordedStringHttpBody.getContent("UTF-8"));
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testGetByteArrayUnsupportEncoding()
      throws Exception {
    String str = "Hello world";
    RecordedStringHttpBody recordedStringHttpBody = new RecordedStringHttpBody(str);
    recordedStringHttpBody.getContent("UNKNOWN");
  }
}
