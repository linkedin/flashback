/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

/**
 * CONSTANTS for Scene serialization
 *
 * @author shfeng
 */
public final class SceneSerializationConstant {
  static final String SCENE_TAG_NAME = "NAME";
  static final String SCENE_TAG_HTTPEXCHANGE_LIST = "HTTPEXCHANGELIST";
  static final String SCENE_TAG_HTTPEXCHANGE = "HTTPEXCHANGE";
  static final String SCENE_TAG_UPDATE_TIME = "UPDATETIME";
  static final String SCENE_TAG_HTTPREQUEST = "HTTPREQUEST";
  static final String SCENE_TAG_HTTPRESPONSE = "HTTPRESPONSE";
  static final String SCENE_TAG_HTTPMETHOD = "HTTPMETHOD";
  static final String SCENE_TAG_HTTPURI = "HTTPURI";
  static final String SCENE_TAG_HTTPHEADERS = "HTTPHEADERS";
  static final String SCENE_TAG_STRING_HTTPBODY = "STRINGHTTPBODY";
  static final String SCENE_TAG_BINARY_HTTPBODY = "BINARYHTTPBODY";
  static final String SCENE_TAG_ENCODED_HTTPBODY = "ENCODEDHTTPBODY";
  static final String SCENE_TAG_HTTPBODY_ENCODING = "HTTPBODYENCODING";
  static final String SCENE_TAG_HTTPSTATUS_CODE = "HTTPSTATUSCODE";
  static final String FILE_CHARSET = "UTF-8";

  private SceneSerializationConstant() {
  }
}
