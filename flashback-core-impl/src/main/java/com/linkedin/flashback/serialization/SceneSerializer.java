/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.serializable.RecordedByteHttpBody;
import com.linkedin.flashback.serializable.RecordedEncodedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpExchange;
import com.linkedin.flashback.serializable.RecordedHttpMessage;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;


/**
 * This class is used for writing, reading scene and serializing{@link com.linkedin.flashback.scene.Scene}
 * Explicit serialization via Streaming API: http://wiki.fasterxml.com/JacksonInFiveMinutes
 *
 * @author shfeng
 * @author dvinegra
 */
public class SceneSerializer {
  private JsonGenerator _jsonGenerator;
  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  public void serialize(Scene scene, Writer writer)
      throws IOException {
    _jsonGenerator = JSON_FACTORY.createGenerator(writer);
    _jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
    _jsonGenerator.writeStartObject();
    _jsonGenerator.writeStringField(SceneSerializationConstant.SCENE_TAG_NAME, scene.getName());
    writeHttpExchanges(scene.getRecordedHttpExchangeList());
    _jsonGenerator.writeEndObject();
    _jsonGenerator.close();
  }

  private void writeHttpExchanges(List<RecordedHttpExchange> recordedHttpExchanges)
      throws IOException {
    _jsonGenerator.writeObjectFieldStart(SceneSerializationConstant.SCENE_TAG_HTTPEXCHANGE_LIST);
    int count = 1;
    for (RecordedHttpExchange recordedHttpExchange : recordedHttpExchanges) {
      _jsonGenerator.writeObjectFieldStart(SceneSerializationConstant.SCENE_TAG_HTTPEXCHANGE + count);
      writeHttpExchange(recordedHttpExchange);
      _jsonGenerator.writeEndObject();
      count++;
    }
    _jsonGenerator.writeEndObject();
  }

  private void writeHttpExchange(RecordedHttpExchange recordedHttpExchanges)
      throws IOException {

    _jsonGenerator.writeStringField(SceneSerializationConstant.SCENE_TAG_UPDATE_TIME,
        recordedHttpExchanges.getUpdateTime().toGMTString());
    _jsonGenerator.writeObjectFieldStart(SceneSerializationConstant.SCENE_TAG_HTTPREQUEST);
    writeHttpRequest(recordedHttpExchanges.getRecordedHttpRequest());
    _jsonGenerator.writeEndObject();

    _jsonGenerator.writeObjectFieldStart(SceneSerializationConstant.SCENE_TAG_HTTPRESPONSE);
    writeHttpResponse(recordedHttpExchanges.getRecordedHttpResponse());
    _jsonGenerator.writeEndObject();
  }

  private void writeHttpRequest(RecordedHttpRequest recordedHttpRequest)
      throws IOException {
    _jsonGenerator
        .writeStringField(SceneSerializationConstant.SCENE_TAG_HTTPMETHOD, recordedHttpRequest.getMethod().toString());
    _jsonGenerator
        .writeStringField(SceneSerializationConstant.SCENE_TAG_HTTPURI, recordedHttpRequest.getUri().toString());
    writeHttpMessage(recordedHttpRequest);
  }

  private void writeHttpResponse(RecordedHttpResponse recordedHttpResponse)
      throws IOException {
    _jsonGenerator
        .writeNumberField(SceneSerializationConstant.SCENE_TAG_HTTPSTATUS_CODE, recordedHttpResponse.getStatus());
    writeHttpMessage(recordedHttpResponse);
  }

  private void writeHttpMessage(RecordedHttpMessage recordedHttpMessage)
      throws IOException {
    if (!recordedHttpMessage.getHeaders().isEmpty()) {
      writeHttpHeaders(recordedHttpMessage.getHeaders());
    }
    if (recordedHttpMessage.hasHttpBody()) {
      writeHttpBody(recordedHttpMessage.getHttpBody());
    }
  }

  private void writeHttpHeaders(Map<String, String> headers)
      throws IOException {
    _jsonGenerator.writeObjectFieldStart(SceneSerializationConstant.SCENE_TAG_HTTPHEADERS);
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      _jsonGenerator.writeStringField(entry.getKey(), entry.getValue());
    }
    _jsonGenerator.writeEndObject();
  }

  private void writeHttpBody(RecordedHttpBody httpBody)
      throws IOException {
    if (httpBody instanceof RecordedEncodedHttpBody) {
      _jsonGenerator.writeObjectFieldStart(SceneSerializationConstant.SCENE_TAG_ENCODED_HTTPBODY);
      _jsonGenerator.writeStringField(SceneSerializationConstant.SCENE_TAG_HTTPBODY_ENCODING,
          ((RecordedEncodedHttpBody) httpBody).getEncodingName());
      writeHttpBody(((RecordedEncodedHttpBody) httpBody).getDecodedBody());
      _jsonGenerator.writeEndObject();
    } else if (httpBody instanceof RecordedStringHttpBody) {
      _jsonGenerator.writeStringField(SceneSerializationConstant.SCENE_TAG_STRING_HTTPBODY,
          ((RecordedStringHttpBody) httpBody).getContent());
    } else {
      _jsonGenerator.writeBinaryField(SceneSerializationConstant.SCENE_TAG_BINARY_HTTPBODY,
          ((RecordedByteHttpBody) httpBody).getContent());
    }
  }
}
