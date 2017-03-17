/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.serializable.RecordedByteHttpBody;
import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpExchange;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used for writing, reading scene and de-serializing{@link com.linkedin.flashback.scene.Scene}
 * Explicit serialization via Streaming API: http://wiki.fasterxml.com/JacksonInFiveMinutes
 * @author shfeng
 * @author dvinegra
 */
public class SceneDeserializer {
  private static final JsonFactory JSON_FACTORY = new JsonFactory();
  private JsonParser _jsonParser;

  public Scene deserialize(Reader reader)
      throws IOException {
    _jsonParser = JSON_FACTORY.createParser(reader);
    skipStartObject();
    _jsonParser.nextToken();
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_NAME);
    _jsonParser.nextToken();
    String name = _jsonParser.getValueAsString();
    return new Scene(name, null, ".", createHttpExchangeList());
  }

  private List<RecordedHttpExchange> createHttpExchangeList()
      throws IOException {
    _jsonParser.nextToken();  //HTTPEXCHANGELIST FIELD_NAME
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_HTTPEXCHANGE_LIST);
    skipStartObject(); // HTTPEXCHANGELIST START_OBJECT
    List<RecordedHttpExchange> recordedHttpExchangeList = new ArrayList<>();
    int count = 1;
    _jsonParser.nextToken();  // HTTPEXCHANGE FIELD_NAME
    while (_jsonParser.getCurrentToken() != JsonToken.END_OBJECT) {
      if ((SceneSerializationConstant.SCENE_TAG_HTTPEXCHANGE + count).equals(_jsonParser.getCurrentName())) {
        skipStartObject();
        recordedHttpExchangeList.add(createHttpExchange());
        count++;
        skipEndObject();  // JsonToken.START_OBJECT
      }
    }
    return recordedHttpExchangeList;
  }

  private RecordedHttpExchange createHttpExchange()
      throws IOException {
    _jsonParser.nextToken();  // UPDATETIME FIELD_NAME
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_UPDATE_TIME);
    _jsonParser.nextToken();  // UPDATETIME VALUE_STRING
    Date date = new Date(_jsonParser.getValueAsString());
    RecordedHttpRequest recordedHttpRequest = createHttpRequest();
    RecordedHttpResponse recordedHttpResponse = createHttpResponse();
    skipEndObject();  // HTTPEXCHANGE
    return new RecordedHttpExchange(recordedHttpRequest, recordedHttpResponse, date);
  }

  private RecordedHttpRequest createHttpRequest()
      throws IOException {
    _jsonParser.nextToken();  // HTTPREQUEST.FIELD_NAME
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_HTTPREQUEST);
    _jsonParser.nextToken();  // HTTPREQUEST START_OBJECT

    String httpMethod = createHttpMethod();
    URI uri = createUri();

    //Create optional fields
    _jsonParser.nextToken();  // Move to next token
    Map<String, String> headers = createHeaders();
    RecordedHttpBody recordedHttpBody = createHttpBody();
    RecordedHttpRequest recordedHttpRequest = new RecordedHttpRequest(httpMethod, uri, headers, recordedHttpBody);
    //Check if reach to the end of object for http request
    if (!(_jsonParser.getCurrentName().equals(SceneSerializationConstant.SCENE_TAG_HTTPREQUEST)
        && _jsonParser.getCurrentToken() == JsonToken.END_OBJECT)) {
      _jsonParser.nextToken();
    }
    return recordedHttpRequest;
  }

  private URI createUri()
      throws IOException {
    _jsonParser.nextToken();  // HTTPURI FIELD_NAME
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_HTTPURI);
    _jsonParser.nextToken();  // HTTPURI VALUE_STRING
    URI uri;
    try {
      uri = new URI(_jsonParser.getValueAsString());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Failed to construct URI: " + _jsonParser.getValueAsString());
    }
    return uri;
  }

  private String createHttpMethod()
      throws IOException {
    _jsonParser.nextToken();  // HTTPMETHOD FIELD_NAME
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_HTTPMETHOD);
    _jsonParser.nextToken();  // HTTPMETHOD VALUE_STRING
    return _jsonParser.getValueAsString();
  }

  private RecordedHttpResponse createHttpResponse()
      throws IOException {
    _jsonParser.nextToken();  // HTTPRESPONSE Field name
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_HTTPRESPONSE);
    _jsonParser.nextToken();  // HTTPRESPONSE.START_OBJECT

    int statusCode = createStatusCode();

    _jsonParser.nextToken();  // Move to next token
    Map<String, String> headers = createHeaders();
    RecordedHttpBody recordedHttpBody = createHttpBody();
    RecordedHttpResponse recordedHttpResponse = new RecordedHttpResponse(statusCode, headers, recordedHttpBody);

    //Check if reach to the end of object for http response
    if (!(SceneSerializationConstant.SCENE_TAG_HTTPRESPONSE.equals(_jsonParser.getCurrentName())
        && _jsonParser.getCurrentToken() == JsonToken.END_OBJECT)) {
      _jsonParser.nextToken();
    }
    return recordedHttpResponse;
  }

  private int createStatusCode()
      throws IOException {
    _jsonParser.nextToken();  // HTTPSTATUSCODE Filed
    validateRequiredField(SceneSerializationConstant.SCENE_TAG_HTTPSTATUS_CODE);
    _jsonParser.nextToken();  //HTTPSTATUSCODE VALUE_NUMBER_INT

    return _jsonParser.getValueAsInt();
  }

  private Map<String, String> createHeaders()
      throws IOException {
    if (!isValidOptionalField(SceneSerializationConstant.SCENE_TAG_HTTPHEADERS)) {
      return null;
    }
    skipStartObject();  // HTTPHEADERS

    Map<String, String> headers = new HashMap<>();
    while (_jsonParser.nextToken() != JsonToken.END_OBJECT) {   //FIELD_NAME
      String key = _jsonParser.getCurrentName();
      _jsonParser.nextToken();   // VALUE_STRING
      String value = _jsonParser.getValueAsString();
      headers.put(key, value);
    }
    if (SceneSerializationConstant.SCENE_TAG_HTTPHEADERS.equals(_jsonParser.getCurrentName())
        && _jsonParser.getCurrentToken() == JsonToken.END_OBJECT) {
      skipEndObject();
    }
    return headers;
  }

  private RecordedHttpBody createHttpBody()
      throws IOException {
    if (isValidOptionalField(SceneSerializationConstant.SCENE_TAG_ENCODED_HTTPBODY)) {
      skipStartObject();  // ENCODEDHTTPBODY
      _jsonParser.nextToken();  // HTTPBODYENCODING Field
      validateRequiredField(SceneSerializationConstant.SCENE_TAG_HTTPBODY_ENCODING);
      _jsonParser.nextToken();  // HTTPBODYENCODING Value
      String encodingName = _jsonParser.getValueAsString();
      _jsonParser.nextToken();
      RecordedHttpBody decodedBody = createHttpBody();  // Read in the "decoded" body content so that we can wrap it
      skipEndObject();
      return new RecordedEncodedHttpBody(decodedBody, encodingName);
    }
    if (isValidOptionalField(SceneSerializationConstant.SCENE_TAG_STRING_HTTPBODY)) {
      _jsonParser.nextToken();  // Field
      return new RecordedStringHttpBody(_jsonParser.getValueAsString());
    }
    if (isValidOptionalField(SceneSerializationConstant.SCENE_TAG_BINARY_HTTPBODY)) {
      _jsonParser.nextToken();  // Field
      return new RecordedByteHttpBody(_jsonParser.getBinaryValue());
    }
    return null;
  }

  private void validateRequiredField(String expectedFiledName)
      throws IOException {
    if (!expectedFiledName.equals(_jsonParser.getCurrentName())) {
      throw new IllegalStateException("Unrecognized field '" + _jsonParser.getCurrentName() + "'!");
    }
  }

  private boolean isValidOptionalField(String expectedFiledName)
      throws IOException {
    return expectedFiledName.equals(_jsonParser.getCurrentName());
  }

  private void skipStartObject()
      throws IOException {
    _jsonParser.nextToken(); // JsonToken.START_OBJECT
  }

  private void skipEndObject()
      throws IOException {
    _jsonParser.nextToken(); // JsonToken.END_OBJECT
  }
}
