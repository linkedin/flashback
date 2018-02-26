/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import com.google.common.base.Charsets;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;


/**
 * Abstract Recorded http message(Java bean) which contains properties that is used for both
 * http request and http response
 * @author shfeng
 * @author dvinegra
 */
public abstract class RecordedHttpMessage {
  private static final String DEFAULT_CHARSET = Charsets.UTF_8.toString();
  private static final Logger logger = Logger.getLogger("RecordedHttpMessage");
  private static final String DEFAULT_CONTENT_TYPE = MediaType.OCTET_STREAM.toString();

  private Multimap<String, String> _headers = LinkedHashMultimap.create();
  private RecordedHttpBody _httpBody;

  public RecordedHttpMessage(Multimap<String, String> headers, RecordedHttpBody httpBody) {
    if (headers != null) {
      _headers = headers;
    }
    _httpBody = httpBody;

    // Update the Content-Length header if appropriate
    if (_headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
      try {
        int contentLength = _httpBody.getContent(getCharset()).length;
        _headers = LinkedHashMultimap.create(_headers);
        _headers.put(HttpHeaders.CONTENT_LENGTH, Integer.toString(contentLength));
      } catch (IOException e) {
        logger.error("Caught exception " + e + " while updating Content-Length header");
      }
    }
  }

  public RecordedHttpBody getHttpBody() {
    return _httpBody;
  }

  public boolean hasHttpBody() {
    return _httpBody != null;
  }

  public Multimap<String, String> getHeaders() {
    return _headers;
  }

  public String getCharset() {
    // Content_Type cannot have multiple, commas-separated values, so this is safe.
    Iterator<String> header = _headers.get(HttpHeaders.CONTENT_TYPE).iterator();
    if (!header.hasNext()) {
      return DEFAULT_CHARSET;
    } else {
      return MediaType.parse(header.next()).charset().or(Charsets.UTF_8).toString();
    }
  }

  public String getContentType() {
    // Content_Type cannot have multiple, commas-separated values, so this is safe.
    Iterator<String> header = _headers.get(HttpHeaders.CONTENT_TYPE).iterator();
    if (!header.hasNext()) {
      return DEFAULT_CONTENT_TYPE;
    } else {
      return MediaType.parse(header.next()).withoutParameters().toString();
    }
  }
}
