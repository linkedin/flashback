/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import com.linkedin.flashback.decorator.compression.AbstractCompressor;
import com.linkedin.flashback.decorator.compression.AbstractDecompressor;
import com.linkedin.flashback.decorator.compression.DeflateCompressor;
import com.linkedin.flashback.decorator.compression.DeflateDecompressor;
import com.linkedin.flashback.decorator.compression.GzipCompressor;
import com.linkedin.flashback.decorator.compression.GzipDecompressor;
import com.linkedin.flashback.http.HttpUtilities;
import java.io.IOException;


/**
 * This class provides an abstraction for an HTTP body that is encoded using
 * gzip or deflate encoding.
 *
 * Internally, a RecordedEncodedHttpBody needs to store a RecordedHttpBody representing
 * the decoded (uncompressed) HTTP body content, as well as the name of the encoding used.
 * When asked to provide the byte array representing the content to send over the wire, the
 * decoded body is encoded (compressed) and the result is returned. Additionally, this compressed
 * value is cached so that the uncompressed body only needs to be compressed once.
 *
 * @author dvinegra
 */
public class RecordedEncodedHttpBody implements RecordedHttpBody {

  private final RecordedHttpBody _decodedBody;
  private final String _encodingName;

  // Cached the encoded content (to send on the wire) so that we don't compress multiple times
  private byte[] _encodedContent;

  /**
   * Constructor used to create a RecordedHttpBody instance from an already-decoded RecordedHttpBody
   *
   * @param decodedBody underlying RecordedHttpBody representing the body bytes *after* decoding
   * @param encodingName name of the encoding used to encode the content
   */
  public RecordedEncodedHttpBody(RecordedHttpBody decodedBody, String encodingName) {
    _decodedBody = decodedBody;
    _encodingName = encodingName;
  }

  /**
   * Constructor used to create a RecordedEncodedHttpBody instance from the encoded (wire) bytes
   *
   * @param encodedContent the already encoded content, representing the bytes transferred over the wire
   * @param encodingName name of the encoding used to encode the content
   * @param charset charset
   * @param contentType http Content-Type for the content (once decoded)
   * @throws java.io.IOException
   */
  public RecordedEncodedHttpBody(byte[] encodedContent, String encodingName, String charset, String contentType)
      throws IOException {
    _encodedContent = encodedContent;
    _encodingName = encodingName;

    byte[] decodedContent = getDecompressor().decompress(encodedContent);

    if (HttpUtilities.isTextContentType(contentType)) {
      _decodedBody = new RecordedStringHttpBody(new String(decodedContent, charset));
    } else {
      _decodedBody = new RecordedByteHttpBody(decodedContent);
    }
  }

  /**
   * Returns the bytes meant to be sent over the wire. If necessary, this method
   * will encode the underlying http body content and cache the result so that
   * we don't have to do this more than once.
   *
   * @param charSet  charset
   * @return the (encoded) content for the wire
   * @throws java.io.IOException
   */
  @Override
  public byte[] getContent(String charSet)
      throws IOException {
    if (_encodedContent == null) {
      // Only compress the content once and cache the result
      _encodedContent = getCompressor().compress(_decodedBody.getContent(charSet));
    }
    return _encodedContent;
  }

  public String getEncodingName() {
    return _encodingName;
  }

  public RecordedHttpBody getDecodedBody() {
    return _decodedBody;
  }

  private AbstractCompressor getCompressor() {
    if (HttpUtilities.GZIP_CONSTANT.equals(_encodingName)) {
      return new GzipCompressor();
    } else if (HttpUtilities.DEFLATE_CONSTANT.equals(_encodingName)) {
      return new DeflateCompressor();
    } else {
      throw new IllegalStateException("Invalid encoding for RecordedEncodedHttpBody");
    }
  }

  private AbstractDecompressor getDecompressor() {
    if (HttpUtilities.GZIP_CONSTANT.equals(_encodingName)) {
      return new GzipDecompressor();
    } else if (HttpUtilities.DEFLATE_CONSTANT.equals(_encodingName)) {
      return new DeflateDecompressor();
    } else {
      throw new IllegalStateException("Invalid encoding for RecordedEncodedHttpBody");
    }
  }
}
