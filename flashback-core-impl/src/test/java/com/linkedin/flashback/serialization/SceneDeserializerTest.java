/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

import com.linkedin.flashback.scene.Scene;
import java.io.StringReader;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class SceneDeserializerTest {
  @Test
  public void testDeserialize()
      throws Exception {
    StringReader stringReader = new StringReader(MockDataGenerator.getSerializedScene());
    SceneDeserializer sceneDeserializer = new SceneDeserializer();
    Scene scene = sceneDeserializer.deserialize(stringReader);
    Scene expectedScene = MockDataGenerator.getMockScene();
    Assert.assertEquals(scene, expectedScene);
  }

  @Test
  public void testDeserializeWithoutHeaders()
      throws Exception {
    StringReader stringReader = new StringReader(MockDataGenerator.getSerializedSceneWithoutHeaders());
    SceneDeserializer sceneDeserializer = new SceneDeserializer();
    Scene scene = sceneDeserializer.deserialize(stringReader);
    Scene expectedScene = MockDataGenerator.getMockSceneWithoutHeaders();
    Assert.assertEquals(scene, expectedScene);
  }

  @Test
  public void testDeserializeWithoutBody()
      throws Exception {
    StringReader stringReader = new StringReader(MockDataGenerator.getSerializedSceneWithoutBody());
    SceneDeserializer sceneDeserializer = new SceneDeserializer();
    Scene scene = sceneDeserializer.deserialize(stringReader);
    Scene expectedScene = MockDataGenerator.getMockSceneWithoutBody();
    Assert.assertEquals(scene, expectedScene);
  }

  @Test
  public void testDeserializeWithoutBodyAndHeaders()
      throws Exception {
    StringReader stringReader = new StringReader(MockDataGenerator.getSerializedSceneWithoutBodyAndHeader());
    SceneDeserializer sceneDeserializer = new SceneDeserializer();
    Scene scene = sceneDeserializer.deserialize(stringReader);
    Scene expectedScene = MockDataGenerator.getMockSceneWithoutBodyAndHeader();
    Assert.assertEquals(scene, expectedScene);
  }
}
