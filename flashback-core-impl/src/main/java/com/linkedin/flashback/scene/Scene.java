/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.scene;

import com.linkedin.flashback.serializable.RecordedHttpExchange;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 * Scene that contains everything that will be stored in scene file.
 * Note: The best practice to create Scene is using SceneFactory
 *
 * @author shfeng
 */
public class Scene {
  private final String _name;
  private final List<RecordedHttpExchange> _recordedHttpExchangeList;
  private final SceneMode _sceneMode;
  private final String _sceneRoot;

  public Scene(String name, SceneMode sceneMode, String sceneRoot,
      List<RecordedHttpExchange> recordedHttpExchangeList) {
    _name = name;
    _sceneMode = sceneMode;
    _sceneRoot = sceneRoot;
    _recordedHttpExchangeList = recordedHttpExchangeList;
  }

  public Scene(SceneConfiguration sceneConfiguration) {
    this(sceneConfiguration.getSceneName(), sceneConfiguration.getSceneMode(), sceneConfiguration.getSceneRoot(),
        new ArrayList<>());
  }

  public List<RecordedHttpExchange> getRecordedHttpExchangeList() {
    return _recordedHttpExchangeList;
  }

  public String getName() {
    return _name;
  }

  public boolean isReadable() {
    return _sceneMode == SceneMode.PLAYBACK || _sceneMode == SceneMode.SEQUENTIAL_PLAYBACK;
  }

  public boolean isSequential() {
    return _sceneMode == SceneMode.SEQUENTIAL_RECORD || _sceneMode == SceneMode.SEQUENTIAL_PLAYBACK;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String getSceneRoot() {
    return _sceneRoot;
  }
}
