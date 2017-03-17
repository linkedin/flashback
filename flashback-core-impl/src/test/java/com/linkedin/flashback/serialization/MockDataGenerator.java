/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.serializable.RecordedByteHttpBody;
import com.linkedin.flashback.serializable.RecordedHttpExchange;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.serializable.RecordedStringHttpBody;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * TODO: move to external resource files
 * @author shfeng
 */
public class MockDataGenerator {

  static Scene getMockScene()
      throws URISyntaxException {

    Map<String, String> requestHeaders1 = new HashMap<>();
    requestHeaders1.put("request-header1", "value1");
    requestHeaders1.put("request-header2", "value2, value3, value4");
    Map<String, String> responseHeaders1 = new HashMap<>();
    responseHeaders1.put("response-header1", "value1");
    responseHeaders1.put("response-header2", "value2, value3, value4");

    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("GET", new URI("https://www.google.com"), requestHeaders1,
            new RecordedStringHttpBody("Hello world request is awesome"));
    RecordedHttpResponse recordedHttpResponse1 =
        new RecordedHttpResponse(200, responseHeaders1, new RecordedStringHttpBody("Hello world response is awesome"));
    RecordedHttpExchange recordedHttpExchange1 =
        new RecordedHttpExchange(recordedHttpRequest1, recordedHttpResponse1, new Date("2 Oct 2015 21:04:49 GMT"));

    Map<String, String> requestHeaders2 = new HashMap<>();
    requestHeaders2.put("request-header11", "value11");
    requestHeaders2.put("request-header12", "value12, value13, value14");
    Map<String, String> responseHeaders2 = new HashMap<>();
    responseHeaders2.put("response-header11", "value11");
    responseHeaders2.put("response-header12", "value21, value31, value41");

    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("GET", new URI("https://www.yahoo.com"), requestHeaders2,
            new RecordedStringHttpBody("Hello world request is awesome"));
    byte[] bytes = {1, 2, 3, 4, 5};
    RecordedHttpResponse recordedHttpResponse2 =
        new RecordedHttpResponse(200, responseHeaders2, new RecordedByteHttpBody(bytes));
    RecordedHttpExchange recordedHttpExchange2 =
        new RecordedHttpExchange(recordedHttpRequest2, recordedHttpResponse2, new Date("2 Oct 2015 21:04:49 GMT"));

    List<RecordedHttpExchange> recordedHttpExchangeList = new ArrayList<>();
    recordedHttpExchangeList.add(recordedHttpExchange1);
    recordedHttpExchangeList.add(recordedHttpExchange2);
    return new Scene("testing", null, ".", recordedHttpExchangeList);
  }

  static String getSerializedScene() {
    return "{\n" + "  \"NAME\" : \"testing\",\n" + "  \"HTTPEXCHANGELIST\" : {\n" + "    \"HTTPEXCHANGE1\" : {\n"
        + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n" + "      \"HTTPREQUEST\" : {\n"
        + "        \"HTTPMETHOD\" : \"GET\",\n" + "        \"HTTPURI\" : \"https://www.google.com\",\n"
        + "        \"HTTPHEADERS\" : {\n" + "          \"request-header2\" : \"value2, value3, value4\",\n"
        + "          \"request-header1\" : \"value1\"\n" + "        },\n"
        + "        \"STRINGHTTPBODY\" : \"Hello world request is awesome\"\n" + "      },\n" + "      \"HTTPRESPONSE\" : {\n"
        + "        \"HTTPSTATUSCODE\" : 200,\n" + "        \"HTTPHEADERS\" : {\n"
        + "          \"response-header2\" : \"value2, value3, value4\",\n"
        + "          \"response-header1\" : \"value1\"\n" + "        },\n"
        + "        \"STRINGHTTPBODY\" : \"Hello world response is awesome\"\n" + "      }\n" + "    },\n"
        + "    \"HTTPEXCHANGE2\" : {\n" + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n"
        + "      \"HTTPREQUEST\" : {\n" + "        \"HTTPMETHOD\" : \"GET\",\n"
        + "        \"HTTPURI\" : \"https://www.yahoo.com\",\n" + "        \"HTTPHEADERS\" : {\n"
        + "          \"request-header12\" : \"value12, value13, value14\",\n"
        + "          \"request-header11\" : \"value11\"\n" + "        },\n"
        + "        \"STRINGHTTPBODY\" : \"Hello world request is awesome\"\n" + "      },\n" + "      \"HTTPRESPONSE\" : {\n"
        + "        \"HTTPSTATUSCODE\" : 200,\n" + "        \"HTTPHEADERS\" : {\n"
        + "          \"response-header12\" : \"value21, value31, value41\",\n"
        + "          \"response-header11\" : \"value11\"\n" + "        },\n"
        + "        \"BINARYHTTPBODY\" : \"AQIDBAU=\"\n" + "      }\n" + "    }\n" + "  }\n" + "}";
  }

  static Scene getMockSceneWithoutHeaders()
      throws URISyntaxException {
    RecordedHttpRequest recordedHttpRequest1 = new RecordedHttpRequest("GET", new URI("https://www.google.com"), null,
        new RecordedStringHttpBody("Hello world request is awesome"));
    RecordedHttpResponse recordedHttpResponse1 =
        new RecordedHttpResponse(200, null, new RecordedStringHttpBody("Hello world response is awesome"));
    RecordedHttpExchange recordedHttpExchange1 =
        new RecordedHttpExchange(recordedHttpRequest1, recordedHttpResponse1, new Date("2 Oct 2015 21:04:49 GMT"));

    RecordedHttpRequest recordedHttpRequest2 = new RecordedHttpRequest("GET", new URI("https://www.yahoo.com"), null,
        new RecordedStringHttpBody("Hello world request is awesome"));
    byte[] bytes = {1, 2, 3, 4, 5};
    RecordedHttpResponse recordedHttpResponse2 = new RecordedHttpResponse(200, null, new RecordedByteHttpBody(bytes));
    RecordedHttpExchange recordedHttpExchange2 =
        new RecordedHttpExchange(recordedHttpRequest2, recordedHttpResponse2, new Date("2 Oct 2015 21:04:49 GMT"));

    List<RecordedHttpExchange> recordedHttpExchangeList = new ArrayList<>();
    recordedHttpExchangeList.add(recordedHttpExchange1);
    recordedHttpExchangeList.add(recordedHttpExchange2);
    return new Scene("testing", null, ".", recordedHttpExchangeList);
  }

  static String getSerializedSceneWithoutHeaders() {
    return "{\n" + "  \"NAME\" : \"testing\",\n" + "  \"HTTPEXCHANGELIST\" : {\n" + "    \"HTTPEXCHANGE1\" : {\n"
        + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n" + "      \"HTTPREQUEST\" : {\n"
        + "        \"HTTPMETHOD\" : \"GET\",\n" + "        \"HTTPURI\" : \"https://www.google.com\",\n"
        + "        \"STRINGHTTPBODY\" : \"Hello world request is awesome\"\n" + "      },\n" + "      \"HTTPRESPONSE\" : {\n"
        + "        \"HTTPSTATUSCODE\" : 200,\n" + "        \"STRINGHTTPBODY\" : \"Hello world response is awesome\"\n"
        + "      }\n" + "    },\n" + "    \"HTTPEXCHANGE2\" : {\n"
        + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n" + "      \"HTTPREQUEST\" : {\n"
        + "        \"HTTPMETHOD\" : \"GET\",\n" + "        \"HTTPURI\" : \"https://www.yahoo.com\",\n"
        + "        \"STRINGHTTPBODY\" : \"Hello world request is awesome\"\n" + "      },\n" + "      \"HTTPRESPONSE\" : {\n"
        + "        \"HTTPSTATUSCODE\" : 200,\n" + "        \"BINARYHTTPBODY\" : \"AQIDBAU=\"\n" + "      }\n"
        + "    }\n" + "  }\n" + "}";
  }

  static Scene getMockSceneWithoutBody()
      throws URISyntaxException {
    Map<String, String> requestHeaders1 = new HashMap<>();
    requestHeaders1.put("request-header1", "value1");
    requestHeaders1.put("request-header2", "value2, value3, value4");
    Map<String, String> responseHeaders1 = new HashMap<>();
    responseHeaders1.put("response-header1", "value1");
    responseHeaders1.put("response-header2", "value2, value3, value4");

    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("GET", new URI("https://www.google.com"), requestHeaders1, null);
    RecordedHttpResponse recordedHttpResponse1 = new RecordedHttpResponse(200, responseHeaders1, null);
    RecordedHttpExchange recordedHttpExchange1 =
        new RecordedHttpExchange(recordedHttpRequest1, recordedHttpResponse1, new Date("2 Oct 2015 21:04:49 GMT"));

    Map<String, String> requestHeaders2 = new HashMap<>();
    requestHeaders2.put("request-header11", "value11");
    requestHeaders2.put("request-header12", "value12, value13, value14");
    Map<String, String> responseHeaders2 = new HashMap<>();
    responseHeaders2.put("response-header11", "value11");
    responseHeaders2.put("response-header12", "value21, value31, value41");

    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("GET", new URI("https://www.yahoo.com"), requestHeaders2, null);
    byte[] bytes = {1, 2, 3, 4, 5};
    RecordedHttpResponse recordedHttpResponse2 = new RecordedHttpResponse(200, responseHeaders2, null);
    RecordedHttpExchange recordedHttpExchange2 =
        new RecordedHttpExchange(recordedHttpRequest2, recordedHttpResponse2, new Date("2 Oct 2015 21:04:49 GMT"));

    List<RecordedHttpExchange> recordedHttpExchangeList = new ArrayList<>();
    recordedHttpExchangeList.add(recordedHttpExchange1);
    recordedHttpExchangeList.add(recordedHttpExchange2);
    return new Scene("testing", null, ".", recordedHttpExchangeList);
  }

  static String getSerializedSceneWithoutBody() {
    return "{\n" + "  \"NAME\" : \"testing\",\n" + "  \"HTTPEXCHANGELIST\" : {\n" + "    \"HTTPEXCHANGE1\" : {\n"
        + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n" + "      \"HTTPREQUEST\" : {\n"
        + "        \"HTTPMETHOD\" : \"GET\",\n" + "        \"HTTPURI\" : \"https://www.google.com\",\n"
        + "        \"HTTPHEADERS\" : {\n" + "          \"request-header2\" : \"value2, value3, value4\",\n"
        + "          \"request-header1\" : \"value1\"\n" + "        }\n" + "      },\n" + "      \"HTTPRESPONSE\" : {\n"
        + "        \"HTTPSTATUSCODE\" : 200,\n" + "        \"HTTPHEADERS\" : {\n"
        + "          \"response-header2\" : \"value2, value3, value4\",\n"
        + "          \"response-header1\" : \"value1\"\n" + "        }\n" + "      }\n" + "    },\n"
        + "    \"HTTPEXCHANGE2\" : {\n" + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n"
        + "      \"HTTPREQUEST\" : {\n" + "        \"HTTPMETHOD\" : \"GET\",\n"
        + "        \"HTTPURI\" : \"https://www.yahoo.com\",\n" + "        \"HTTPHEADERS\" : {\n"
        + "          \"request-header12\" : \"value12, value13, value14\",\n"
        + "          \"request-header11\" : \"value11\"\n" + "        }\n" + "      },\n"
        + "      \"HTTPRESPONSE\" : {\n" + "        \"HTTPSTATUSCODE\" : 200,\n" + "        \"HTTPHEADERS\" : {\n"
        + "          \"response-header12\" : \"value21, value31, value41\",\n"
        + "          \"response-header11\" : \"value11\"\n" + "        }\n" + "      }\n" + "    }\n" + "  }\n" + "}";
  }

  static Scene getMockSceneWithoutBodyAndHeader()
      throws URISyntaxException {
    RecordedHttpRequest recordedHttpRequest1 =
        new RecordedHttpRequest("GET", new URI("https://www.google.com"), null, null);
    RecordedHttpResponse recordedHttpResponse1 = new RecordedHttpResponse(200, null, null);
    RecordedHttpExchange recordedHttpExchange1 =
        new RecordedHttpExchange(recordedHttpRequest1, recordedHttpResponse1, new Date("2 Oct 2015 21:04:49 GMT"));

    RecordedHttpRequest recordedHttpRequest2 =
        new RecordedHttpRequest("GET", new URI("https://www.yahoo.com"), null, null);
    byte[] bytes = {1, 2, 3, 4, 5};
    RecordedHttpResponse recordedHttpResponse2 = new RecordedHttpResponse(200, null, null);
    RecordedHttpExchange recordedHttpExchange2 =
        new RecordedHttpExchange(recordedHttpRequest2, recordedHttpResponse2, new Date("2 Oct 2015 21:04:49 GMT"));

    List<RecordedHttpExchange> recordedHttpExchangeList = new ArrayList<>();
    recordedHttpExchangeList.add(recordedHttpExchange1);
    recordedHttpExchangeList.add(recordedHttpExchange2);
    return new Scene("testing", null, ".", recordedHttpExchangeList);
  }

  static String getSerializedSceneWithoutBodyAndHeader() {
    return "{\n" + "  \"NAME\" : \"testing\",\n" + "  \"HTTPEXCHANGELIST\" : {\n" + "    \"HTTPEXCHANGE1\" : {\n"
        + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n" + "      \"HTTPREQUEST\" : {\n"
        + "        \"HTTPMETHOD\" : \"GET\",\n" + "        \"HTTPURI\" : \"https://www.google.com\"\n" + "      },\n"
        + "      \"HTTPRESPONSE\" : {\n" + "        \"HTTPSTATUSCODE\" : 200\n" + "      }\n" + "    },\n"
        + "    \"HTTPEXCHANGE2\" : {\n" + "      \"UPDATETIME\" : \"2 Oct 2015 21:04:49 GMT\",\n"
        + "      \"HTTPREQUEST\" : {\n" + "        \"HTTPMETHOD\" : \"GET\",\n"
        + "        \"HTTPURI\" : \"https://www.yahoo.com\"\n" + "      },\n" + "      \"HTTPRESPONSE\" : {\n"
        + "        \"HTTPSTATUSCODE\" : 200\n" + "      }\n" + "    }\n" + "  }\n" + "}";
  }
}
