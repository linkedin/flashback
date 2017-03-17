/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.scene;

import com.linkedin.flashback.serializable.RecordedHttpExchange;
import java.util.List;


/**
 * Create a dummy scene for proxy server to start with, then
 * the user will need to set their specific scene as need.
 */
public class DummyScene extends Scene {
  static final String SCENE_IS_NOT_VALID = "scene is not valid";

  public DummyScene() {
    super(null, null, null, null);
  }

  @Override
  public List<RecordedHttpExchange> getRecordedHttpExchangeList() {
    throw new IllegalStateException(SCENE_IS_NOT_VALID);
  }

  @Override
  public String getName() {
    throw new IllegalStateException(SCENE_IS_NOT_VALID);
  }

  @Override
  public boolean isReadable() {
    throw new IllegalStateException(SCENE_IS_NOT_VALID);
  }

  @Override
  public boolean isSequential() {
    throw new IllegalStateException(SCENE_IS_NOT_VALID);
  }

  @Override
  public int hashCode() {
    throw new IllegalStateException(SCENE_IS_NOT_VALID);
  }

  @Override
  public boolean equals(Object obj) {
    throw new IllegalStateException(SCENE_IS_NOT_VALID);
  }

  @Override
  public String getSceneRoot() {
    throw new IllegalStateException(SCENE_IS_NOT_VALID);
  }
}
