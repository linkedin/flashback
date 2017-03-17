/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback;

import com.google.common.collect.Iterables;
import com.linkedin.flashback.matchrules.DummyMatchRule;
import com.linkedin.flashback.matchrules.MatchRule;
import com.linkedin.flashback.scene.DummyScene;
import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.serializable.RecordedHttpExchange;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.serialization.SceneWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Provide functionality to read, write and lookup RecordedHttpExchange to scene.
 * Also, it allows client to change scene at running time.
 *
 * @author shfeng
 * @author dvinegra
 */
public class SceneAccessLayer {
  static final String THE_SCENE_IS_NOT_READABLE = "the scene is not readable";
  static final String SCENE_IS_NOT_ALLOWED_BE_NULL = "scene is not allowed to be null";
  static final String MATCHRULE_IS_NOT_ALLOWED_BE_NULL = "matchrule is not allowed to be null";
  static final String SCENEWRITER_IS_NOT_ALLOWED_BE_NULL = "scenewriter is not allowed to be null";
  static final String NO_MATCHING_RECORDING_FOUND = "no matching recording found";
  static final String FAILED_TO_WRITE_SCENE_TO_THE_FILE = "Failed to write scene to the file";

  private SceneWriter _sceneWriter;
  private Scene _scene;
  private MatchRule _matchRule;
  private int _sequencePosition = 0;
  private boolean _dirty = false;

  public SceneAccessLayer(Scene scene, SceneWriter sceneWriter, MatchRule matchRule) {
    if (scene == null) {
      throw new IllegalArgumentException(SCENE_IS_NOT_ALLOWED_BE_NULL);
    }

    if (sceneWriter == null) {
      throw new IllegalArgumentException(SCENEWRITER_IS_NOT_ALLOWED_BE_NULL);
    }

    if (matchRule == null) {
      throw new IllegalArgumentException(MATCHRULE_IS_NOT_ALLOWED_BE_NULL);
    }

    _sceneWriter = sceneWriter;
    _matchRule = matchRule;
    _scene = scene;
  }

  public SceneAccessLayer(Scene scene, MatchRule matchRule) {
    this(scene, new SceneWriter(), matchRule);
  }

  public SceneAccessLayer() {
    this(new DummyScene(), new SceneWriter(), new DummyMatchRule());
  }

  public String getSceneName() {
    return _scene.getName();
  }

  /**
   * set match rule
   * */
  public void setMatchRule(MatchRule matchRule) {
    if (matchRule == null) {
      throw new IllegalArgumentException(MATCHRULE_IS_NOT_ALLOWED_BE_NULL);
    }
    _matchRule = matchRule;
  }

  /**
   * set scene if client need use switch scenes at run time.
   *
   * */
  public void setScene(Scene scene) {
    if (scene == null) {
      throw new IllegalArgumentException(SCENE_IS_NOT_ALLOWED_BE_NULL);
    }
    flush();
    _scene = scene;
    _sequencePosition = 0;
  }

  public boolean canPlayback() {
    return _scene.isReadable();
  }

  /**
   * Check if incoming http request matches any HttpExchange from the scene
   * @param request incoming request from client
   * @return true if found matched request, otherwise return false
   *
   * */
  public boolean hasMatchRequest(RecordedHttpRequest request) {
    return findMatchRequest(request) >= 0;
  }

  /**
   * Given incoming http request, find matched response from the scene and return response from the scene
   * @param request http request from client
   * @return matched http response from the scene
   *
   * */
  public RecordedHttpResponse playback(RecordedHttpRequest request) {
    if (!_scene.isReadable()) {
      throw new IllegalStateException(THE_SCENE_IS_NOT_READABLE);
    }
    int position = findMatchRequest(request);
    if (position < 0) {
      throw new IllegalStateException(NO_MATCHING_RECORDING_FOUND);
    }
    if (_scene.isSequential()) {
      _sequencePosition++;
    }
    List<RecordedHttpExchange> recordedHttpExchangeList = _scene.getRecordedHttpExchangeList();
    return recordedHttpExchangeList.get(position).getRecordedHttpResponse();
  }

  /**
   * Record request and response to the scene. Updates will be performed in-memory and will be written to disk
   * when flush() is called, or when the Scene is changed.
   * @param recordedHttpRequest http request from client
   * @param recordedHttpResponse http response from upstream service
   *
   * */
  public void record(RecordedHttpRequest recordedHttpRequest, RecordedHttpResponse recordedHttpResponse) {
    List<RecordedHttpExchange> recordedHttpExchangeList = _scene.getRecordedHttpExchangeList();
    RecordedHttpExchange recordedHttpExchange =
        new RecordedHttpExchange(recordedHttpRequest, recordedHttpResponse, new Date());
    if (!_scene.isSequential()) {
      int position = findMatchRequest(recordedHttpRequest);
      if (position >= 0) {
        recordedHttpExchangeList.set(position, recordedHttpExchange);
      } else {
        recordedHttpExchangeList.add(recordedHttpExchange);
      }
    } else {
      recordedHttpExchangeList.add(recordedHttpExchange);
    }
    _dirty = true;
  }

  /**
   * Serialize the scene to disk, if it has been updated
   */
  public void flush() {
    if (_dirty) {
      try {
        _sceneWriter.writeScene(_scene);
        _dirty = false;
      } catch (IOException e) {
        throw new RuntimeException(FAILED_TO_WRITE_SCENE_TO_THE_FILE, e);
      }
    }
  }

  /**
   * produces a string description for the match failure reason for a particular request
   * @param request incoming request that we are trying to match
   * @return a String describing the match failure reasons for the request
   */
  public String getMatchFailureDescription(RecordedHttpRequest request) {
    List<String> failureDescriptionList = new ArrayList<>();
    List<RecordedHttpExchange> exchangeList = _scene.getRecordedHttpExchangeList();
    if (_scene.isSequential()) {
      if (_sequencePosition < exchangeList.size()) {
        failureDescriptionList.add(_matchRule.getMatchFailureDescriptionForRequests(request, exchangeList.get(_sequencePosition).getRecordedHttpRequest()));
      } else {
        failureDescriptionList.add("No more recorded requests in sequential scene");
      }
    } else {
      for (int i = 0; i < exchangeList.size(); i++) {
        RecordedHttpExchange exchange = exchangeList.get(i);
        failureDescriptionList.add(String.format("Recorded Request %d:%n%s", i + 1,
            _matchRule.getMatchFailureDescriptionForRequests(request, exchange.getRecordedHttpRequest())));
      }
    }
    return new StringBuilder().append("Could not find matching request in scene " + _scene.getName() + "%n")
        .append(String.join("%n", failureDescriptionList))
        .toString();
  }

  /**
   * find matched request from scene
   * @param request incoming request that we'd like match in existing scene
   * @return position of list of HttpExchanges from the scene. return -1 if no match found
   *
   * */
  private int findMatchRequest(final RecordedHttpRequest request) {
    if (_scene.isSequential()) {
      List<RecordedHttpExchange> exchangeList = _scene.getRecordedHttpExchangeList();
      // In sequential playback mode, only test the request at the current sequence index
      if (_sequencePosition < exchangeList.size() && _matchRule
          .test(request, exchangeList.get(_sequencePosition).getRecordedHttpRequest())) {
        return _sequencePosition;
      }
      return -1;
    } else {
      return Iterables.indexOf(_scene.getRecordedHttpExchangeList(),
          input -> _matchRule.test(request, input.getRecordedHttpRequest()));
    }
  }
}
