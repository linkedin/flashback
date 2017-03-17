/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.dataflow;

import com.linkedin.mitm.proxy.channel.ChannelMediator;
import io.netty.handler.codec.http.HttpObject;


/**
 * This is the interface that controls 1) proxy mode that indicate
 * direction of data flow 2) what data to send out or store somewhere
 *
 * @author shfeng
 */
public interface ProxyModeController {
  /**
   * Decide if request should be sent out to server using channelMediator
   * or sent response back to client using channelMediator
   *
   * @param channelMediator it knows how to communicate to client/server.
   * @param httpObject the initial data from {@link com.linkedin.mitm.proxy.channel.ClientChannelHandler}
   *
   */
  void handleReadFromClient(final ChannelMediator channelMediator, final HttpObject httpObject);

  /**
   * Manipulate outgoing request to server
   *
   * @param httpObject outgoing request
   * @return final httpObject that need to be wrote to server
   */
  default HttpObject handleWriteToServer(HttpObject httpObject) {
    return httpObject;
  }

  /**
   * Could be used to store incoming response
   *
   * @param httpObject incoming response from server
   */
  default void handleReadFromServer(final HttpObject httpObject) {
  }

  /**
   * Manipulate outgoing response to client
   *
   * @param httpObject outgoing response that is about to send back to client
   * @return final httpObject that need to be written to server
   */
  default HttpObject handleWriteToClient(HttpObject httpObject) {
    return httpObject;
  }
}
