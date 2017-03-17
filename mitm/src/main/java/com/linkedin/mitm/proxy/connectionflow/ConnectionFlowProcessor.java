/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.connectionflow;

import com.linkedin.mitm.proxy.channel.ChannelMediator;
import com.linkedin.mitm.proxy.channel.Flushable;
import com.linkedin.mitm.proxy.connectionflow.steps.ConnectionFlowStep;
import io.netty.handler.codec.http.HttpRequest;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


/**
 * Connection flow processor class which implemented common
 * connection flow processing logic for both Http and Https.
 * It's stateful. New instance get created every time we execute one connection flow.
 * It holds immutable connectionFlow which is defined at proxy boostrap
 * and maintained the state of connectionFlow execution.
 *
 * @author shfeng
 */
public class ConnectionFlowProcessor {
  private static final String MODULE = ConnectionFlowProcessor.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private static final Pattern HTTP_PREFIX = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);
  private final ChannelMediator _channelMediator;
  private final List<ConnectionFlowStep> _connectionFlow;
  private final long _startTime;
  private final InetSocketAddress _remoteAddress;
  private boolean _complete;
  private int _stepIndex = 0;
  private Flushable _flushable;

  public ConnectionFlowProcessor(ChannelMediator channelMediator, HttpRequest httpRequest,
      List<ConnectionFlowStep> connectionFlow) {
    _channelMediator = channelMediator;
    _connectionFlow = connectionFlow;
    _complete = false;
    _startTime = System.currentTimeMillis();
    _remoteAddress = getRemoteAddress(httpRequest);
    LOG.debug(String.format("Start connection flow at: %d", _startTime));
  }

  /**
   * Check if connection flow is completed or not
   * */
  public boolean isComplete() {
    return _complete;
  }

  /**
   * Start connection flow  and execute callback one by one
   * */
  public void startConnectionFlow(Flushable flushable) {
    _flushable = flushable;
    nextStep();
  }

  /**
   * Process next connection flow step
   * */
  public void nextStep() {
    if (_connectionFlow.size() == _stepIndex) {
      //succeed, should notify all of threads waiting for this connection.
      LOG.debug(String.format("Finished connection flow in: %d ms", System.currentTimeMillis() - _startTime));
      _complete = true;
      _flushable.flush();
    } else {
      ConnectionFlowStep connectionFlowStep = _connectionFlow.get(_stepIndex++);
      process(connectionFlowStep);
    }
  }

  /**
   * Process this connection flow step
   * */
  private void process(ConnectionFlowStep connectionFlowStep) {
    connectionFlowStep.execute(_channelMediator, _remoteAddress).addListener(future -> {
      if (future.isSuccess()) {
        LOG.debug("Finished processing at" + System.currentTimeMillis());
        nextStep();
      } else {
        //TODO: send back 503 before close channel.
        _channelMediator.disconnectBothChannels();
      }
    });
  }

  /**
   * Resolve remote address
   * */
  private static InetSocketAddress getRemoteAddress(HttpRequest httpRequest) {
    String uri = httpRequest.getUri();
    String uriWithoutProtocol;
    if (HTTP_PREFIX.matcher(uri).matches()) {
      uriWithoutProtocol = StringUtils.substringAfter(uri, "://");
    } else {
      uriWithoutProtocol = uri;
    }
    String hostAndPort;
    if (uriWithoutProtocol.contains("/")) {
      hostAndPort = uriWithoutProtocol.substring(0, uriWithoutProtocol.indexOf("/"));
    } else {
      hostAndPort = uriWithoutProtocol;
    }
    String hostName;
    int port;
    if (hostAndPort.contains(":")) {
      int index = hostAndPort.indexOf(":");
      hostName = hostAndPort.substring(0, index);
      port = Integer.parseInt(hostAndPort.substring(index + 1));
    } else {
      hostName = hostAndPort;
      port = 80;
    }
    return new InetSocketAddress(hostName, port);
  }
}
