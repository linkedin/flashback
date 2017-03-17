/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;


/**
 * Common utilities and constants pertaining to HTTP headers
 *
 * @author dvinegra
 */
public final class HttpUtilities {

  private static final String IS_TEXT_REGEX_CONSTANT = "^text/|application/(json|javascript|(\\w+\\+)?xml)";
  private static final String FORM_URL_ENCODED_CONSTANT = "application/x-www-form-urlencoded";
  public final static String GZIP_CONSTANT = "gzip";
  public final static String DEFLATE_CONSTANT = "deflate";
  public final static String UTF8_CONSTANT = "UTF-8";
  public final static String HTTP_SCHEME = "http";
  public final static String HTTPS_SCHEME = "https";
  public final static int HTTP_DEFAULT_PORT = 80;
  public final static int HTTPS_DEFAULT_PORT = 443;

  private HttpUtilities() {
  }

  /**
   * Check if content is text content type
   * @param contentType http body content type
   *
   * */
  static public boolean isTextContentType(String contentType) {
    return contentType != null && (Pattern.compile(IS_TEXT_REGEX_CONSTANT).matcher(contentType).find()
        || isFormURLEncodedContentType(contentType));
  }

  /**
   * Check if content is application/x-www-form-url-encoded (POST parameters)
   * @param contentType http body content type
   *
   */
  static public boolean isFormURLEncodedContentType(String contentType) {
    return FORM_URL_ENCODED_CONSTANT.equals(contentType);
  }

  /**
   * Check if content is encoded in a compression format
   * @param encodingName http body content encoding
   *
   * */
  static public boolean isCompressedContentEncoding(String encodingName) {
    return GZIP_CONSTANT.equals(encodingName) || DEFLATE_CONSTANT.equals(encodingName);
  }

  /**
   * Converts a URL / POST parameter string to an ordered map of key / value pairs
   * @param paramsString the URL-encoded &-delimited string of key / value pairs
   * @return a LinkedHashMap representing the decoded parameters
   */
  static public Map<String, String> stringToUrlParams(String paramsString, String charset)
      throws UnsupportedEncodingException {
    Map<String, String> params = new LinkedHashMap<>();
    if (!StringUtils.isBlank(paramsString)) {
      for (String param : paramsString.split("&")) {
        String[] keyValue = param.split("=");
        assert (keyValue.length > 0);
        if (keyValue.length == 1) {
          params.put(URLDecoder.decode(keyValue[0], charset), "");
        } else {
          params.put(URLDecoder.decode(keyValue[0], charset), URLDecoder.decode(keyValue[1], charset));
        }
      }
    }
    return params;
  }

  /**
   * Converts an ordered map of key / value pairs to a URL parameter string
   * @param params the map of key / value pairs
   * @return a string representation of the key / value pairs, delimited by '&'
   */
  static public String urlParamsToString(Map<String, String> params, String charset)
      throws UnsupportedEncodingException {
    if (params.size() == 0) {
      return "";
    }
    StringBuilder resultBuilder = new StringBuilder();
    for (Map.Entry<String, String> entry : params.entrySet()) {
      String encodedName = URLEncoder.encode(entry.getKey(), charset);
      String encodedValue = URLEncoder.encode(entry.getValue(), charset);
      resultBuilder.append(encodedName).append("=").append(encodedValue).append("&");
    }
    resultBuilder.deleteCharAt(resultBuilder.length() - 1);
    return resultBuilder.toString();
  }
}
