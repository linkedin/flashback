/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software  
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.linkedin.flashback.smartproxy;

import com.linkedin.flashback.SceneAccessLayer;
import com.linkedin.flashback.factory.SceneFactory;
import com.linkedin.flashback.matchrules.MatchRuleUtils;
import com.linkedin.flashback.scene.SceneConfiguration;
import com.linkedin.flashback.scene.SceneMode;
import java.io.IOException;
import java.net.URL;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * This test is temporary class that cover some basic case for Http request.
 * Need find a good way to test Https without expose CA certificate in flashback repository
 * @author shfeng
 */
public class FlashbackRunnerTest {
  private static final String FLASHBACK_SCENE_DIR = "/flashback/scene";
  private static final String PROXY_HOST = "localhost";
  private static final int PROXY_PORT = 5555;
  private static final String HTTP_SCENE = "http";
  private static final SceneMode SCENE_MODE = SceneMode.PLAYBACK;

  @Test()
  public void testReplayHttp() throws InterruptedException, IOException {
    URL flashbackScene = getClass().getResource(FLASHBACK_SCENE_DIR);
    String rootPath = flashbackScene.getPath();
    SceneConfiguration sceneConfiguration = new SceneConfiguration(rootPath, SCENE_MODE, HTTP_SCENE);
    try (FlashbackRunner flashbackRunner = new FlashbackRunner.Builder().mode(SCENE_MODE)
        .sceneAccessLayer(
            new SceneAccessLayer(SceneFactory.create(sceneConfiguration), MatchRuleUtils.matchEntireRequest()))
        .build()) {
      flashbackRunner.start();
      HttpHost host = new HttpHost(PROXY_HOST, PROXY_PORT);
      String url = "http://www.example.org/";
      HttpClient client = HttpClientBuilder.create().setProxy(host).build();
      HttpGet request = new HttpGet(url);
      HttpResponse httpResponse0 = client.execute(request);
      Assert.assertTrue(EntityUtils.toString(httpResponse0.getEntity())
          .contains("I am from Flashback scene, not http://example.org"));

      url = "http://www.nba.com/";
      request = new HttpGet(url);
      HttpResponse httpResponse1 = client.execute(request);
      Assert.assertTrue(EntityUtils.toString(httpResponse1.getEntity())
          .contains("I am from Flashback scene, not http://www.nba.com"));

      url = "http://www.notexist.org/";
      request = new HttpGet(url);
      HttpResponse httpResponse2 = client.execute(request);
      Assert.assertEquals(httpResponse2.getStatusLine().getStatusCode(), 400);
      Assert.assertTrue(EntityUtils.toString(httpResponse2.getEntity())
          .contains("No Matching Request"));

    }
  }

  @Test
  public void testNotMatchUrl() throws IOException, InterruptedException {
    URL flashbackScene = getClass().getResource(FLASHBACK_SCENE_DIR);
    String rootPath = flashbackScene.getPath();
    SceneConfiguration sceneConfiguration = new SceneConfiguration(rootPath, SCENE_MODE, HTTP_SCENE);
    try (FlashbackRunner flashbackRunner = new FlashbackRunner.Builder().mode(SCENE_MODE)
        .sceneAccessLayer(
            new SceneAccessLayer(SceneFactory.create(sceneConfiguration), MatchRuleUtils.matchEntireRequest()))
        .build()) {
      flashbackRunner.start();
      HttpHost host = new HttpHost(PROXY_HOST, PROXY_PORT);
      String url = "http://www.notexist.org/";
      HttpClient client = HttpClientBuilder.create().setProxy(host).build();
      HttpGet request = new HttpGet(url);
      HttpResponse httpResponse = client.execute(request);
      Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 400);
      Assert.assertTrue(EntityUtils.toString(httpResponse.getEntity())
          .contains("No Matching Request"));

      url = "http://www.example.org/";
      request = new HttpGet(url);
      request.addHeader("a", "b");
      httpResponse = client.execute(request);
      Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 400);
      Assert.assertTrue(EntityUtils.toString(httpResponse.getEntity())
          .contains("No Matching Request"));

    }
  }

  @Test
  public void testNotMatchHeaders() throws IOException, InterruptedException {
    URL flashbackScene = getClass().getResource(FLASHBACK_SCENE_DIR);
    String rootPath = flashbackScene.getPath();
    SceneConfiguration sceneConfiguration = new SceneConfiguration(rootPath, SCENE_MODE, HTTP_SCENE);
    try (FlashbackRunner flashbackRunner = new FlashbackRunner.Builder().mode(SCENE_MODE)
        .sceneAccessLayer(
            new SceneAccessLayer(SceneFactory.create(sceneConfiguration), MatchRuleUtils.matchEntireRequest()))
        .build()) {
      flashbackRunner.start();
      HttpHost host = new HttpHost(PROXY_HOST, PROXY_PORT);
      String url = "http://www.example.org/";
      HttpClient client = HttpClientBuilder.create().setProxy(host).build();
      HttpGet request = new HttpGet(url);
      request.addHeader("a", "b");
      HttpResponse httpResponse = client.execute(request);
      Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 400);
      Assert.assertTrue(EntityUtils.toString(httpResponse.getEntity())
          .contains("No Matching Request"));
    }
  }

  @Test
  public void testNotMatchMethod() throws IOException, InterruptedException {
    URL flashbackScene = getClass().getResource(FLASHBACK_SCENE_DIR);
    String rootPath = flashbackScene.getPath();
    SceneConfiguration sceneConfiguration = new SceneConfiguration(rootPath, SCENE_MODE, HTTP_SCENE);
    try (FlashbackRunner flashbackRunner = new FlashbackRunner.Builder().mode(SCENE_MODE)
        .sceneAccessLayer(
            new SceneAccessLayer(SceneFactory.create(sceneConfiguration), MatchRuleUtils.matchEntireRequest()))
        .build()) {
      flashbackRunner.start();
      HttpHost host = new HttpHost(PROXY_HOST, PROXY_PORT);
      String url = "http://www.example.org/";
      HttpClient client = HttpClientBuilder.create().setProxy(host).build();
      HttpPost post = new HttpPost(url);
      HttpResponse httpResponse = client.execute(post);
      Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 400);
      Assert.assertTrue(EntityUtils.toString(httpResponse.getEntity())
          .contains("No Matching Request"));
    }
  }

  @Test
  public void testSetCookieHeader() throws IOException, InterruptedException {
    URL flashbackScene = getClass().getResource(FLASHBACK_SCENE_DIR);
    String rootPath = flashbackScene.getPath();
    SceneConfiguration sceneConfiguration = new SceneConfiguration(rootPath, SCENE_MODE, "setCookie");
    try (FlashbackRunner flashbackRunner = new FlashbackRunner.Builder().mode(SCENE_MODE)
        .sceneAccessLayer(
            new SceneAccessLayer(SceneFactory.create(sceneConfiguration), MatchRuleUtils.matchEntireRequest()))
        .build()) {
      flashbackRunner.start();
      HttpHost host = new HttpHost(PROXY_HOST, PROXY_PORT);
      String url = "http://www.example.org/";
      HttpClient client = HttpClientBuilder.create().setProxy(host).build();
      HttpGet request = new HttpGet(url);
      request.addHeader("Set-Cookie",
              "ABC=\\\"R:0|g:fcaa967e-asdfa-484a-8a5e-asdfa\\\"; Version=1; Max-Age=30; Expires=Thu, 23-Mar-2017 18:01:20 GMT; Path=/");
      request.addHeader("Set-Cookie",
              "ABC=\\\"R:0|g:fcaa967e-asdfasdf-484a-8a5e-asdf|n:asdfasdfasd-37ca-42cf-a909-95e0dd19e334\\\"; Version=1; Max-Age=30; Expires=Thu, 23-Mar-2017 18:01:20 GMT; Path=/");
      request.addHeader("Set-Cookie",
              "ABC=\\\"R:0|i:138507\\\"; Version=1; Max-Age=30; Expires=Thu, 23-Mar-2017 18:01:20 GMT; Path=/");
      request.addHeader("Set-Cookie",
              "ABC=\\\"R:0|i:138507|e:42\\\"; Version=1; Max-Age=30; Expires=Thu, 23-Mar-2017 18:01:20 GMT; Path=/");
      request.addHeader("Set-Cookie", "guestidc=0d28bda6-5d42-4ee9-bd1e-asdasda; Domain=asdafsdfasdfasdfa.com; Path=/");

      HttpResponse httpResponse0 = client.execute(request);
      String strEnt = EntityUtils.toString(httpResponse0.getEntity());
      Assert.assertTrue(strEnt
          .contains("I am from Flashback scene, not http://example.org"));

    }
  }
}
