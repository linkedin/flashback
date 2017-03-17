/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.scene;

/**
 * Scene mode: either record only or playback only
 *
 * @author shfeng
 */
public enum SceneMode {
  RECORD("record"),
  PLAYBACK("playback"),
  SEQUENTIAL_RECORD("sequential_record"),
  SEQUENTIAL_PLAYBACK("sequential_playback");

  private final String _text;
  SceneMode(String text) {
    _text = text;
  }

  public static SceneMode fromString(String text) {
    if (text != null) {
      for (SceneMode sceneMode : SceneMode.values()) {
        if (text.equalsIgnoreCase(sceneMode._text)) {
          return sceneMode;
        }
      }
    }
    return null;
  }
}
