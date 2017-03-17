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
import java.util.Date;
import java.util.List;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;


/**
 * @author shfeng
 */
public class SceneFactoryTest {
  private static final String NAME = "name";
  private static final String ROOT = "root";
  private SceneReader _sceneReader;

  @Test
  public void testCreateSceneExistInPlaybackMode()
      throws Exception {
    Scene expectedScene = EasyMock.createStrictMock(Scene.class);

    List<RecordedHttpExchange> recordedHttpExchanges = new ArrayList<>();
    Date now = new Date();
    recordedHttpExchanges.add(new RecordedHttpExchange(null, null, now));
    EasyMock.expect(expectedScene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchanges);
    EasyMock.replay(expectedScene);

    Scene result = runTestGetResult(SceneMode.PLAYBACK, expectedScene);

    Assert.assertNotNull(result);
    Assert.assertEquals(result.getName(), NAME);
    Assert.assertEquals(result.getSceneRoot(), ROOT);
    Assert.assertTrue(result.isReadable());
    Assert.assertEquals(result.getRecordedHttpExchangeList().get(0).getUpdateTime(), now);
  }

  @Test
  public void testCreateSceneExistInSequentialRecordMode()
      throws Exception {
    Scene expectedScene = EasyMock.createStrictMock(Scene.class);
    List<RecordedHttpExchange> recordedHttpExchanges = new ArrayList<>();
    Date now = new Date();
    recordedHttpExchanges.add(new RecordedHttpExchange(null, null, now));
    EasyMock.expect(expectedScene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchanges);
    EasyMock.replay(expectedScene);
    Scene result = runTestGetResult(SceneMode.SEQUENTIAL_RECORD, expectedScene);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getName(), NAME);
    Assert.assertEquals(result.getSceneRoot(), ROOT);
    Assert.assertFalse(result.isReadable());
    // Creating a Scene in sequential record mode should clear the list of exchanges to allow for re-recording
    Assert.assertEquals(result.getRecordedHttpExchangeList().size(), 0);
  }

  @Test
  public void testCreateSceneExistInRecordMode()
      throws Exception {
    Scene expectedScene = EasyMock.createStrictMock(Scene.class);
    List<RecordedHttpExchange> recordedHttpExchanges = new ArrayList<>();
    Date now = new Date();
    recordedHttpExchanges.add(new RecordedHttpExchange(null, null, now));
    EasyMock.expect(expectedScene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchanges);
    EasyMock.replay(expectedScene);
    Scene result = runTestGetResult(SceneMode.RECORD, expectedScene);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getName(), NAME);
    Assert.assertEquals(result.getSceneRoot(), ROOT);
    Assert.assertFalse(result.isReadable());
    Assert.assertEquals(result.getRecordedHttpExchangeList().get(0).getUpdateTime(), now);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testCreateSceneNotExistInPlaybackMode()
      throws Exception {
    runTestGetResult(SceneMode.PLAYBACK, null);
  }

  @Test
  public void testCreateSceneNotExistInRecordMode()
      throws Exception {
    Scene result = runTestGetResult(SceneMode.RECORD, null);
    Assert.assertNotNull(result);
    Assert.assertEquals(result.getName(), NAME);
    Assert.assertEquals(result.getSceneRoot(), ROOT);
    Assert.assertFalse(result.isReadable());
  }

  private Scene runTestGetResult(SceneMode scenMode, Scene expectedScene)
      throws IOException {
    _sceneReader = EasyMock.createStrictMock(SceneReader.class);
    SceneConfiguration sceneConfiguration = new SceneConfiguration(ROOT, scenMode, NAME);
    EasyMock.expect(_sceneReader.readScene(EasyMock.anyString(), EasyMock.anyString())).andReturn(expectedScene);

    EasyMock.replay(_sceneReader);
    Scene result = SceneFactory.create(sceneConfiguration, _sceneReader);
    return result;
  }

  @AfterMethod
  public void shutdown() {
    EasyMock.verify(_sceneReader);
  }
}
