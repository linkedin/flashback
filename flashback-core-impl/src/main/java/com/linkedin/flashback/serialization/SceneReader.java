/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

import com.google.common.io.Files;
import com.linkedin.flashback.scene.Scene;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;


/**
 * Read de-serialized scene from file.
 *
 * @author shfeng
 */
public class SceneReader {
  /**
   * Read scene from file and construct Scene object
   * @param name scene name
   * @return scene object de-serialized from file
   *
   * */
  public Scene readScene(String rootPath, String name)
      throws IOException {
    File file = new File(rootPath, name);
    if (file.isFile()) {
      if (file.length() == 0) {
        return new Scene(name, null, rootPath, new ArrayList<>());
      }
      BufferedReader reader = Files.newReader(file, Charset.forName(SceneSerializationConstant.FILE_CHARSET));
      SceneDeserializer sceneDeserializer = new SceneDeserializer();
      return sceneDeserializer.deserialize(reader);
    }
    return null;
  }
}
