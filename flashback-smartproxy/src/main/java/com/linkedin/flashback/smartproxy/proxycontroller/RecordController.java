/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.smartproxy.proxycontroller;

import com.linkedin.flashback.SceneAccessLayer;
import com.linkedin.flashback.netty.builder.RecordedHttpRequestBuilder;
import com.linkedin.flashback.netty.builder.RecordedHttpResponseBuilder;
import com.linkedin.mitm.proxy.channel.ChannelMediator;
import com.linkedin.mitm.proxy.dataflow.ProxyModeController;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.IOException;
import org.apache.log4j.Logger;


/**
 * Record controller which record original http request and store in files.
 * New instance gets created for each new connection coming.
 *
 * @author shfeng
 */
public class RecordController implements ProxyModeController {
  private static final Logger LOG = Logger.getLogger(RecordController.class);

  private final RecordedHttpRequestBuilder _clientRequestBuilder;
  private RecordedHttpResponseBuilder _serverResponseBuilder;
  private final SceneAccessLayer _sceneAccessLayer;

  public RecordController(SceneAccessLayer sceneAccessLayer, HttpRequest httpRequest) {
    _clientRequestBuilder = new RecordedHttpRequestBuilder(httpRequest);
    _sceneAccessLayer = sceneAccessLayer;
  }

  @Override
  public void handleReadFromClient(ChannelMediator channelMediator, HttpObject httpObject) {
    if (channelMediator == null) {
      throw new IllegalStateException("HRFC: ChannelMediator can't be null");
    }

    try {
      if (httpObject instanceof HttpRequest) {
        HttpRequest httpRequest = (HttpRequest) httpObject;
        _clientRequestBuilder.interpretHttpRequest(httpRequest);
        _clientRequestBuilder.addHeaders(httpRequest);
      }

      if (httpObject instanceof HttpContent) {
        _clientRequestBuilder.appendHttpContent((HttpContent) httpObject);
      }
    } catch (IOException e) {
      throw new RuntimeException("HRFC: Failed to record HttpContent", e);
    }

    channelMediator.writeToServer(httpObject);
  }

  @Override
  public void handleReadFromServer(HttpObject httpObject) {
    if (httpObject instanceof HttpResponse) {
      _serverResponseBuilder = new RecordedHttpResponseBuilder((HttpResponse) httpObject);
    }

    try {
      if (httpObject instanceof HttpContent) {
        _serverResponseBuilder.appendHttpContent((HttpContent) httpObject);
      }

      if (httpObject instanceof LastHttpContent) {
        _sceneAccessLayer.record(_clientRequestBuilder.build(), _serverResponseBuilder.build());
      }
    } catch (IOException e) {
      throw new RuntimeException("HRFS: Failed to record HttpContent", e);
    }
  }
}
