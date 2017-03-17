/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.factory;

import com.linkedin.flashback.http.HttpUtilities;
import com.linkedin.flashback.serializable.RecordedByteHttpBody;
import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;


/**
 * Implementation of Http body factory class
 * @author shfeng
 * @author dvinegra
 */
public final class RecordedHttpBodyFactory {

  private RecordedHttpBodyFactory() {
  }

  /**
   * Given content type, charset, construct concrete {@link com.linkedin.flashback.serializable.RecordedByteHttpBody}
   * @param contentType http body content type
   * @param contentEncoding http body content encoding
   * @param inputStream input stream from http request/response
   * @param charset charset
   * @return concrete {@link com.linkedin.flashback.serializable.RecordedHttpBody}
   *
   * */
  public static RecordedHttpBody create(String contentType, String contentEncoding, final InputStream inputStream,
      final String charset)
      throws IOException {

    if (HttpUtilities.isCompressedContentEncoding(contentEncoding)) {
      return new RecordedEncodedHttpBody(IOUtils.toByteArray(inputStream), contentEncoding, charset,
          contentType);
    } else if (HttpUtilities.isTextContentType(contentType)) {
      return new RecordedStringHttpBody(IOUtils.toString(inputStream, charset));
    } else {
      return new RecordedByteHttpBody(IOUtils.toByteArray(inputStream));
    }
  }
}
