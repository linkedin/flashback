/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

import com.google.common.io.Files;
import com.linkedin.flashback.scene.Scene;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;


/**
 * Write serialized scene to file.
 *
 * @author shfeng
 */
public class SceneWriter {
  /**
   * Store scene in file
   * */
  public void writeScene(Scene scene)
      throws IOException {
    File file = new File(scene.getSceneRoot(), scene.getName());
    File parent = file.getParentFile();
    if (!parent.exists() && !parent.mkdirs()) {
      throw new IllegalStateException("Failed to create new directory: " + parent);
    }
    BufferedWriter bufferedWriter = Files.newWriter(file, Charset.forName(SceneSerializationConstant.FILE_CHARSET));
    SceneSerializer sceneSerializer = new SceneSerializer();
    sceneSerializer.serialize(scene, bufferedWriter);
  }
}
