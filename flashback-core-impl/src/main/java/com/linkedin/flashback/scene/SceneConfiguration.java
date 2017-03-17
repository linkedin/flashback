/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.scene;

/**
 * SceneConfiguration that contains all necessary settings for recording and replaying.
 *
 * @author shfeng
 */
public class SceneConfiguration {
  private final String _sceneRoot;
  private final SceneMode _sceneMode;
  private final String _sceneName;

  public SceneConfiguration(String sceneRoot, SceneMode sceneMode, String sceneName) {
    _sceneRoot = sceneRoot;
    _sceneMode = sceneMode;
    _sceneName = sceneName;
  }

  /**
   * Get Scene mode: record or replay
   * */
  public SceneMode getSceneMode() {
    return _sceneMode;
  }

  /**
   * Get root path of loading/storing scene.
   * */
  public String getSceneRoot() {
    return _sceneRoot;
  }

  /**
   * Get name of scene.
   * */
  public String getSceneName() {
    return _sceneName;
  }
}
