/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback;

import com.linkedin.flashback.matchrules.MatchRule;
import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.scene.SceneConfiguration;
import com.linkedin.flashback.scene.SceneMode;
import com.linkedin.flashback.serializable.RecordedHttpExchange;
import com.linkedin.flashback.serializable.RecordedHttpRequest;
import com.linkedin.flashback.serializable.RecordedHttpResponse;
import com.linkedin.flashback.serialization.SceneWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * @author shfeng
 * @author dvinegra
 */
public class SceneAccessLayerTest {
  private static final String SCENE_NAME = "testing";
  private static final String ROOT_PATH = "root";

  @Test
  public void testCanReplayScene() {
    SceneConfiguration sceneConfiguration1 = new SceneConfiguration(ROOT_PATH, SceneMode.PLAYBACK, SCENE_NAME);
    Scene scene1 = new Scene(sceneConfiguration1);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene1, EasyMock.createStrictMock(SceneWriter.class),
        EasyMock.createStrictMock(MatchRule.class));
    Assert.assertTrue(sceneAccessLayer.canPlayback());
    Scene scene2 = new Scene(new SceneConfiguration(ROOT_PATH, SceneMode.RECORD, SCENE_NAME));

    sceneAccessLayer.setScene(scene2);
    Assert.assertFalse(sceneAccessLayer.canPlayback());

    Scene scene3 = new Scene(new SceneConfiguration(ROOT_PATH, SceneMode.SEQUENTIAL_PLAYBACK, SCENE_NAME));
    sceneAccessLayer.setScene(scene3);
    Assert.assertTrue(sceneAccessLayer.canPlayback());

    Scene scene4 = new Scene(new SceneConfiguration(ROOT_PATH, SceneMode.SEQUENTIAL_RECORD, SCENE_NAME));
    sceneAccessLayer.setScene(scene4);
    Assert.assertFalse(sceneAccessLayer.canPlayback());
  }

  @Test
  public void testHasMatchRequest()
      throws URISyntaxException, IOException {
    Scene scene = EasyMock.createStrictMock(Scene.class);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);
    EasyMock.expect(scene.isSequential()).andReturn(false);

    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(true);
    EasyMock.replay(scene, recordedHttpExchange1, recordedHttpRequest1, incomingHttpRequest, matchRule);

    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);

    Assert.assertTrue(sceneAccessLayer.hasMatchRequest(incomingHttpRequest));
    EasyMock.verify(scene, recordedHttpExchange1, recordedHttpRequest1, incomingHttpRequest, matchRule);
  }

  @Test
  public void testHasNoMatchRequest()
      throws URISyntaxException, IOException {
    Scene scene = EasyMock.createStrictMock(Scene.class);
    EasyMock.expect(scene.isSequential()).andReturn(false);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpRequest recordedHttpRequest2 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpRequest()).andReturn(recordedHttpRequest2);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(false);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest2)).andReturn(false);

    EasyMock.replay(scene, recordedHttpExchange1, recordedHttpExchange2, recordedHttpRequest1, recordedHttpRequest2,
        incomingHttpRequest, matchRule);

    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);

    Assert.assertFalse(sceneAccessLayer.hasMatchRequest(incomingHttpRequest));
    EasyMock.verify(scene, recordedHttpExchange1, recordedHttpExchange2, recordedHttpRequest1, recordedHttpRequest2,
        incomingHttpRequest, matchRule);
  }

  @Test
  public void testPlayback()
      throws IOException {
    Scene scene = EasyMock.createMock(Scene.class);
    EasyMock.expect(scene.isReadable()).andReturn(true).anyTimes();
    EasyMock.expect(scene.isSequential()).andReturn(false).anyTimes();
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpRequest recordedHttpRequest2 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpResponse recordedHttpResponse1 = EasyMock.createStrictMock(RecordedHttpResponse.class);
    RecordedHttpResponse recordedHttpResponse2 = EasyMock.createStrictMock(RecordedHttpResponse.class);

    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpRequest()).andReturn(recordedHttpRequest2);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpResponse()).andReturn(recordedHttpResponse1);
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpResponse()).andReturn(recordedHttpResponse2);

    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList).times(2);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(true);

    EasyMock.replay(scene, recordedHttpExchange1, recordedHttpRequest1, matchRule);

    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);

    Assert.assertEquals(sceneAccessLayer.playback(incomingHttpRequest), recordedHttpResponse1);
    EasyMock.verify(scene, recordedHttpExchange1, recordedHttpRequest1, matchRule);
  }

  @Test
  public void testSequentialPlayback()
      throws URISyntaxException, IOException {
    Scene scene = EasyMock.createMock(Scene.class);
    SceneWriter sceneWriter = EasyMock.createMock(SceneWriter.class);
    EasyMock.expect(scene.isSequential()).andReturn(true).anyTimes();
    EasyMock.expect(scene.isReadable()).andReturn(true).anyTimes();

    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createMock(RecordedHttpRequest.class);
    RecordedHttpRequest recordedHttpRequest2 = EasyMock.createMock(RecordedHttpRequest.class);
    RecordedHttpResponse recordedHttpResponse1 = EasyMock.createMock(RecordedHttpResponse.class);
    RecordedHttpResponse recordedHttpResponse2 = EasyMock.createMock(RecordedHttpResponse.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1).anyTimes();
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpRequest()).andReturn(recordedHttpRequest2).anyTimes();
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpResponse()).andReturn(recordedHttpResponse1).anyTimes();
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpResponse()).andReturn(recordedHttpResponse2).anyTimes();

    RecordedHttpRequest incomingHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpRequest incomingHttpRequest2 = EasyMock.createStrictMock(RecordedHttpRequest.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList).anyTimes();

    MatchRule matchRule = EasyMock.createMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest1, recordedHttpRequest1)).andReturn(true).anyTimes();
    EasyMock.expect(matchRule.test(incomingHttpRequest1, recordedHttpRequest2)).andReturn(false).anyTimes();
    EasyMock.expect(matchRule.test(incomingHttpRequest2, recordedHttpRequest1)).andReturn(false).anyTimes();
    EasyMock.expect(matchRule.test(incomingHttpRequest2, recordedHttpRequest2)).andReturn(true).anyTimes();

    EasyMock.replay(scene, recordedHttpExchange1, recordedHttpRequest1, incomingHttpRequest1, recordedHttpExchange2,
        recordedHttpRequest2, incomingHttpRequest2, matchRule);

    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);

    // Should only be able to match the first request in the sequence
    Assert.assertTrue(sceneAccessLayer.hasMatchRequest(incomingHttpRequest1));
    Assert.assertFalse(sceneAccessLayer.hasMatchRequest(incomingHttpRequest2));

    Assert.assertEquals(sceneAccessLayer.playback(incomingHttpRequest1), recordedHttpResponse1);

    // Now that the first request has been played back, only the second request can be matched
    Assert.assertFalse(sceneAccessLayer.hasMatchRequest(incomingHttpRequest1));
    Assert.assertTrue(sceneAccessLayer.hasMatchRequest(incomingHttpRequest2));
    Assert.assertEquals(sceneAccessLayer.playback(incomingHttpRequest2), recordedHttpResponse2);

    EasyMock.verify(scene, recordedHttpExchange1, recordedHttpRequest1, incomingHttpRequest1, matchRule);
  }

  @Test(expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = SceneAccessLayer.THE_SCENE_IS_NOT_READABLE)
  public void testPlaybackNotReadable()
      throws IOException {
    Scene scene = EasyMock.createStrictMock(Scene.class);
    EasyMock.expect(scene.isReadable()).andReturn(false);

    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);
    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);

    EasyMock.replay(scene);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);

    sceneAccessLayer.playback(incomingHttpRequest);
    EasyMock.verify(scene);
  }

  @Test(expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = SceneAccessLayer.NO_MATCHING_RECORDING_FOUND)
  public void testPlaybackNoMatchRequest()
      throws IOException {
    Scene scene = EasyMock.createStrictMock(Scene.class);
    EasyMock.expect(scene.isReadable()).andReturn(true);
    EasyMock.expect(scene.isSequential()).andReturn(false);

    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpRequest recordedHttpRequest2 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpRequest()).andReturn(recordedHttpRequest2);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(false);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest2)).andReturn(false);

    EasyMock.replay(scene, recordedHttpExchange1, recordedHttpExchange2, matchRule);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);
    sceneAccessLayer.playback(incomingHttpRequest);
    EasyMock.verify(scene, recordedHttpExchange1, recordedHttpExchange2, matchRule);
  }

  @Test
  public void testRecordAdd()
      throws IOException {
    Scene scene = EasyMock.createMock(Scene.class);
    EasyMock.expect(scene.isSequential()).andReturn(false).anyTimes();
    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpRequest recordedHttpRequest2 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpRequest()).andReturn(recordedHttpRequest2);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpResponse incomingHttpResponse = EasyMock.createStrictMock(RecordedHttpResponse.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList).times(3);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(false);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest2)).andReturn(false);
    sceneWriter.writeScene(scene);
    EasyMock.expectLastCall();

    EasyMock.replay(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);
    sceneAccessLayer.record(incomingHttpRequest, incomingHttpResponse);
    sceneAccessLayer.flush();
    List<RecordedHttpExchange> recordedHttpExchangeList = scene.getRecordedHttpExchangeList();
    Assert.assertEquals(recordedHttpExchangeList.size(), 3);
    Assert.assertEquals(recordedHttpExchangeList.get(2).getRecordedHttpRequest(), incomingHttpRequest);
    Assert.assertEquals(recordedHttpExchangeList.get(2).getRecordedHttpResponse(), incomingHttpResponse);
    EasyMock.verify(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
  }

  @Test
  public void testRecordAddChangeSceneWithoutExplicitFlush()
      throws IOException {
    Scene scene = EasyMock.createMock(Scene.class);
    EasyMock.expect(scene.isSequential()).andReturn(false).anyTimes();
    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpRequest recordedHttpRequest2 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpRequest()).andReturn(recordedHttpRequest2);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpResponse incomingHttpResponse = EasyMock.createStrictMock(RecordedHttpResponse.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList).times(3);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(false);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest2)).andReturn(false);
    sceneWriter.writeScene(scene);
    EasyMock.expectLastCall();

    EasyMock.replay(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);
    sceneAccessLayer.record(incomingHttpRequest, incomingHttpResponse);

    List<RecordedHttpExchange> recordedHttpExchangeList = scene.getRecordedHttpExchangeList();
    Assert.assertEquals(recordedHttpExchangeList.size(), 3);
    Assert.assertEquals(recordedHttpExchangeList.get(2).getRecordedHttpRequest(), incomingHttpRequest);
    Assert.assertEquals(recordedHttpExchangeList.get(2).getRecordedHttpResponse(), incomingHttpResponse);

    Scene scene2 = EasyMock.createMock(Scene.class);
    sceneAccessLayer.setScene(scene2);

    EasyMock.verify(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
  }

  @Test
  public void testRecordUpdate()
      throws IOException {
    Scene scene = EasyMock.createMock(Scene.class);
    EasyMock.expect(scene.isSequential()).andReturn(false).anyTimes();
    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpResponse incomingHttpResponse = EasyMock.createStrictMock(RecordedHttpResponse.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList).times(3);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(true);
    sceneWriter.writeScene(scene);
    EasyMock.expectLastCall();

    EasyMock.replay(scene, sceneWriter, recordedHttpExchange1, matchRule);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);
    sceneAccessLayer.record(incomingHttpRequest, incomingHttpResponse);
    sceneAccessLayer.flush();
    List<RecordedHttpExchange> recordedHttpExchangeList = scene.getRecordedHttpExchangeList();
    Assert.assertEquals(recordedHttpExchangeList.size(), 2);
    Assert.assertEquals(recordedHttpExchangeList.get(0).getRecordedHttpRequest(), incomingHttpRequest);
    Assert.assertEquals(recordedHttpExchangeList.get(0).getRecordedHttpResponse(), incomingHttpResponse);
    EasyMock.verify(scene, sceneWriter, recordedHttpExchange1, matchRule);
  }

  @Test
  public void testRecordSequential()
      throws IOException {
    Scene scene = EasyMock.createMock(Scene.class);
    EasyMock.expect(scene.isSequential()).andReturn(true).anyTimes();
    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createMock(RecordedHttpRequest.class);
    RecordedHttpRequest recordedHttpRequest2 = EasyMock.createMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1).anyTimes();
    EasyMock.expect(recordedHttpExchange2.getRecordedHttpRequest()).andReturn(recordedHttpRequest2).anyTimes();

    // The test will add the same request (with a different response) and expects that it the new pair is added
    // to the end of the list
    RecordedHttpRequest incomingHttpRequest = recordedHttpRequest1;
    RecordedHttpResponse incomingHttpResponse = EasyMock.createStrictMock(RecordedHttpResponse.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList).times(2);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(true).anyTimes();
    sceneWriter.writeScene(scene);
    EasyMock.expectLastCall();

    EasyMock.replay(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);
    sceneAccessLayer.record(incomingHttpRequest, incomingHttpResponse);
    sceneAccessLayer.flush();
    List<RecordedHttpExchange> recordedHttpExchangeList = scene.getRecordedHttpExchangeList();
    Assert.assertEquals(recordedHttpExchangeList.size(), 3);
    Assert.assertEquals(recordedHttpExchangeList.get(2).getRecordedHttpRequest(), incomingHttpRequest);
    Assert.assertEquals(recordedHttpExchangeList.get(2).getRecordedHttpResponse(), incomingHttpResponse);
    EasyMock.verify(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
  }

  @Test(expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp = SceneAccessLayer.FAILED_TO_WRITE_SCENE_TO_THE_FILE)
  public void testRecordWriteFailure()
      throws IOException {
    Scene scene = EasyMock.createMock(Scene.class);
    EasyMock.expect(scene.isSequential()).andReturn(false).anyTimes();
    RecordedHttpExchange recordedHttpExchange1 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpExchange recordedHttpExchange2 = EasyMock.createStrictMock(RecordedHttpExchange.class);
    RecordedHttpRequest recordedHttpRequest1 = EasyMock.createStrictMock(RecordedHttpRequest.class);
    EasyMock.expect(recordedHttpExchange1.getRecordedHttpRequest()).andReturn(recordedHttpRequest1);
    RecordedHttpRequest incomingHttpRequest = EasyMock.createStrictMock(RecordedHttpRequest.class);
    RecordedHttpResponse incomingHttpResponse = EasyMock.createStrictMock(RecordedHttpResponse.class);

    ArrayList<RecordedHttpExchange> recordedHttpExchangeArrayList = new ArrayList<>();
    recordedHttpExchangeArrayList.add(recordedHttpExchange1);
    recordedHttpExchangeArrayList.add(recordedHttpExchange2);
    EasyMock.expect(scene.getRecordedHttpExchangeList()).andReturn(recordedHttpExchangeArrayList).times(3);
    SceneWriter sceneWriter = EasyMock.createStrictMock(SceneWriter.class);

    MatchRule matchRule = EasyMock.createStrictMock(MatchRule.class);
    EasyMock.expect(matchRule.test(incomingHttpRequest, recordedHttpRequest1)).andReturn(true);
    sceneWriter.writeScene(scene);
    EasyMock.expectLastCall().andThrow(new IOException());

    EasyMock.replay(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
    SceneAccessLayer sceneAccessLayer = new SceneAccessLayer(scene, sceneWriter, matchRule);
    sceneAccessLayer.record(incomingHttpRequest, incomingHttpResponse);
    sceneAccessLayer.flush();
    EasyMock.verify(scene, sceneWriter, recordedHttpExchange1, recordedHttpExchange2, matchRule);
  }
}
