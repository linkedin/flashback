/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.channel.protocol;

import com.linkedin.mitm.proxy.channel.ChannelHandlerDelegate;
import com.linkedin.mitm.proxy.channel.ChannelMediator;
import com.linkedin.mitm.proxy.channel.Flushable;
import com.linkedin.mitm.proxy.connectionflow.ConnectionFlowProcessor;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import java.util.ArrayList;
import java.util.List;


/**
 * Http specific logic to handle reading from Client
 *
 * @author shfeng
 */
public class HttpChannelHandlerDelegate implements ChannelHandlerDelegate {
  private final ConnectionFlowProcessor _connectionFlowProcessor;
  private final ChannelMediator _channelMediator;
  private final ChannelReadCallback _channelReadCallback = new ChannelReadCallback();

  public HttpChannelHandlerDelegate(ChannelMediator channelMediator, ConnectionFlowProcessor httpConnectionFlowProcessor) {
    _channelMediator = channelMediator;
    _connectionFlowProcessor = httpConnectionFlowProcessor;
  }

  @Override
  public void onCreate() {
    _connectionFlowProcessor.startConnectionFlow(_channelReadCallback);
  }

  @Override
  public void onRead(HttpObject httpObject) {
    if (!_connectionFlowProcessor.isComplete()) {
      _channelReadCallback.write(httpObject);
      // Accroding to http://netty.io/wiki/reference-counted-objects.html
      // When an event loop reads data into a ByteBuf and triggers a channelRead() event with it,
      // it is the responsibility of the ChannelHandler in the corresponding pipeline to release the buffer.
      // Since this is the last ChannelHandler, it release the reference-counted after read. So we need to
      // retain to make sure it will not be released until we stored in scene.
      if(httpObject instanceof HttpContent){
        ((HttpContent)httpObject).retain();
      }
      return;
    }
    _channelMediator.readFromClientChannel(httpObject);
  }

  /**
   * Client channel read callback that can hold incoming request and flush them in one shot once connection flow is done.
   * */
  private class ChannelReadCallback implements Flushable {
    private List<HttpObject> _bufferedHttpObjects = new ArrayList<>();

    private void write(HttpObject httpObject) {
      _bufferedHttpObjects.add(httpObject);
    }

    @Override
    public void flush() {
      for (HttpObject httpObject : _bufferedHttpObjects) {
        _channelMediator.readFromClientChannel(httpObject);
      }
      _bufferedHttpObjects.clear();
    }
  }
}
