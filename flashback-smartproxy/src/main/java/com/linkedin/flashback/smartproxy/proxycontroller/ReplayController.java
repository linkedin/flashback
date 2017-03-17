/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.smartproxy.proxycontroller;

import com.linkedin.flashback.SceneAccessLayer;
import com.linkedin.flashback.netty.builder.RecordedHttpRequestBuilder;
import com.linkedin.flashback.netty.mapper.NettyHttpResponseMapper;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.smartproxy.utils.NoMatchResponseGenerator;
import com.linkedin.mitm.proxy.channel.ChannelMediator;
import com.linkedin.mitm.proxy.dataflow.ProxyModeController;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.IOException;
import org.apache.log4j.Logger;


/**
 * Replay controller which playback http response based on matched http request
 * New instance gets created for each new connection coming.
 *
 * @author shfeng
 */
public class ReplayController implements ProxyModeController {
  private static final Logger LOG = Logger.getLogger(ReplayController.class);

  private final RecordedHttpRequestBuilder _clientRequestBuilder;
  private final SceneAccessLayer _sceneAccessLayer;


  public ReplayController(SceneAccessLayer sceneAccessLayer, HttpRequest httpRequest) {
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

      if (httpObject instanceof LastHttpContent) {
        HttpResponse httpResponse = playBack();
        channelMediator.writeToClientAndDisconnect(httpResponse);
      }
    } catch (IOException e) {
      throw new RuntimeException("HRFC: Failed to replay HttpContent", e);
    }
  }

  @Override
  public void handleReadFromServer(HttpObject httpObject) {
     throw new IllegalStateException("No read from server in replay mode");
  }

  /**
   * If found matched request, then return response accordingly.
   * Otherwise, return bad request.
   *
   * @return bad request if not matched request/response found in the scene.
   * */
  private FullHttpResponse playBack()
      throws IOException {
    RecordedHttpRequest recordedHttpRequest = _clientRequestBuilder.build();
    boolean found = _sceneAccessLayer.hasMatchRequest(recordedHttpRequest);
    if (!found) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(_sceneAccessLayer.getMatchFailureDescription(recordedHttpRequest));
      }
      return NoMatchResponseGenerator.generateNoMatchResponse(recordedHttpRequest);
    }
    RecordedHttpResponse recordedHttpResponse = _sceneAccessLayer.playback(recordedHttpRequest);
    return NettyHttpResponseMapper.from(recordedHttpResponse);
  }
}
