/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serialization;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class SceneSerializerTest {
  @Test
  public void testSerialization()
      throws IOException, URISyntaxException {

    SceneSerializer sceneSerializer = new SceneSerializer();
    StringWriter stringWriter = new StringWriter();

    sceneSerializer.serialize(MockDataGenerator.getMockScene(), stringWriter);
    Assert.assertEquals(MockDataGenerator.getSerializedScene(), stringWriter.toString());
  }

  @Test
  public void testSerializationWithoutHeaders()
      throws URISyntaxException, IOException {

    SceneSerializer sceneSerializer = new SceneSerializer();
    StringWriter stringWriter = new StringWriter();

    sceneSerializer.serialize(MockDataGenerator.getMockSceneWithoutHeaders(), stringWriter);
    Assert.assertEquals(MockDataGenerator.getSerializedSceneWithoutHeaders(), stringWriter.toString());
  }

  @Test
  public void testSerializationWithoutBody()
      throws IOException, URISyntaxException {

    SceneSerializer sceneSerializer = new SceneSerializer();
    StringWriter stringWriter = new StringWriter();

    sceneSerializer.serialize(MockDataGenerator.getMockSceneWithoutBody(), stringWriter);
    Assert.assertEquals(MockDataGenerator.getSerializedSceneWithoutBody(), stringWriter.toString());
  }

  @Test
  public void testSerializationWithoutBodyAndHeaders()
      throws IOException, URISyntaxException {

    SceneSerializer sceneSerializer = new SceneSerializer();
    StringWriter stringWriter = new StringWriter();

    sceneSerializer.serialize(MockDataGenerator.getMockSceneWithoutBodyAndHeader(), stringWriter);
    Assert.assertEquals(MockDataGenerator.getSerializedSceneWithoutBodyAndHeader(), stringWriter.toString());
  }
}
