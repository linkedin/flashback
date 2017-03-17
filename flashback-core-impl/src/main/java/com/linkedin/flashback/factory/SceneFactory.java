/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.factory;

import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.scene.SceneConfiguration;
import com.linkedin.flashback.scene.SceneMode;
import com.linkedin.flashback.serializable.RecordedHttpExchange;
import com.linkedin.flashback.serialization.SceneReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Scene factory that create scene based on configuration
 * 1. In playback mode, if found existing scene from file, then playback this scene.
 * 2. In record mode, if found existing scene from the file, then update existing scene.
 * 3. In playback mode, if not found existing scene from the file, then throw exception
 * 4. In record mode, if not found existing scene from the file, then create a new one using configuration
 *
 * @author shfeng
 *
 */
public final class SceneFactory {
  private static final SceneReader SCENE_READER = new SceneReader();

  private SceneFactory() {
  }

  public static Scene create(SceneConfiguration sceneConfiguration)
      throws IOException, IllegalStateException {
    //The reason that pass static variables is that it would be easier to write unit-test
    return create(sceneConfiguration, SCENE_READER);
  }

  /**
   * Helper method that will be used for unit test
   * */
  static Scene create(SceneConfiguration sceneConfiguration, SceneReader sceneReader)
      throws IOException {
    Scene sceneResult = null;
    Scene sceneFromLocal = sceneReader.readScene(sceneConfiguration.getSceneRoot(), sceneConfiguration.getSceneName());
    if (sceneFromLocal == null) {
      if (sceneConfiguration.getSceneMode() == SceneMode.PLAYBACK
          || sceneConfiguration.getSceneMode() == SceneMode.SEQUENTIAL_PLAYBACK) {
        throw new IllegalStateException(String.format("No Scene is found at %s/%s", sceneConfiguration.getSceneRoot(),
            sceneConfiguration.getSceneName()));
      } else {
        sceneResult = new Scene(sceneConfiguration);
      }
    } else {
      List<RecordedHttpExchange> recordedHttpExchanges = sceneFromLocal.getRecordedHttpExchangeList();
      // In sequential record mode, start with an empty scene
      if (sceneConfiguration.getSceneMode() == SceneMode.SEQUENTIAL_RECORD) {
        recordedHttpExchanges = new ArrayList<>();
      }
      sceneResult = new Scene(sceneConfiguration.getSceneName(), sceneConfiguration.getSceneMode(),
          sceneConfiguration.getSceneRoot(), recordedHttpExchanges);
    }

    return sceneResult;
  }
}
