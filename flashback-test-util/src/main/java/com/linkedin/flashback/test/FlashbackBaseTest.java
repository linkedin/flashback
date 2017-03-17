/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.test;

import com.linkedin.flashback.SceneAccessLayer;
import com.linkedin.flashback.factory.SceneFactory;
import com.linkedin.flashback.matchrules.DummyMatchRule;
import com.linkedin.flashback.matchrules.MatchRule;
import com.linkedin.flashback.scene.DummyScene;
import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.scene.SceneConfiguration;
import com.linkedin.flashback.scene.SceneMode;
import com.linkedin.flashback.smartproxy.FlashbackRunner;
import com.linkedin.mitm.model.CertificateAuthority;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;


/**
 * This is the base class that have all the facility of flashback.
 * you could easily extend a test class and have a global flashback instance for use.
 *
 * @author Yabin Kang
 */
public class FlashbackBaseTest {
  private static final String HOME_DIR = System.getProperty("user.home");
  private static final String FLASHBACK_SCENE_DIR = "/flashback/scene";
  /**
   * Proxy settings
   * */
  private String _proxyHost = "localhost";
  private int _proxyPort = 5556;
  /**
   * SSL settings
   * */
  private InputStream _rootCertificateInputStream;
  private String _rootCertificatePassphrase;
  private CertificateAuthority _certificateAuthority;

  private String _defaultSceneName = null;
  private String _defaultScenePath = null;
  private SceneMode _defaultSceneMode = SceneMode.PLAYBACK;
  private Scene _defaultScene = new DummyScene();
  private MatchRule _defaultMatchRule = new DummyMatchRule();
  private static FlashbackRunner _flashbackRunner;

  /**
   * this is the place to override and do anything that you think should be done before the flashback is brought up like change proxy config (a new port).
   */
  @BeforeTest
  protected void flashbackGlobalSetUp()
      throws FileNotFoundException, InterruptedException {
    bootstrap();
  }

  /**
   * this is the place to close flashback or you can override to do something before close the flashback.
   */
  @AfterTest
  protected void flashbackGlobalCleanUp() {
    _flashbackRunner.close();
  }

  /**
   * this is the place to override and change the default scene & matchrule on each test class.
   *
   * @throws IOException
   */
  @BeforeClass
  protected void flashbackTestClassSetUp()
      throws IOException {
    determineDefaultSettings();
  }

  /**
   * Set proxy host. This method need be invoked before
   * flashbackGlobalSetUp method, otherwise proxy will start
   * in the default host
   * @param proxyHost  proxy host
   */
  protected void setProxyHost(String proxyHost) {
    _proxyHost = proxyHost;
  }

  /**
   * Set proxy port. This method need be invoked before
   * flashbackGlobalSetUp method, otherwise proxy will start
   * in the default port
   * @param proxyPort  proxy port
   */
  protected void setProxyPort(int proxyPort) {
    _proxyPort = proxyPort;
  }

  /**
   * Set default scene mode
   * By default, it's playback mode.
   * Changing to record mode to create/update Scene
   * @param sceneMode scene mode
   */
  protected void setSceneMode(SceneMode sceneMode) {
    _defaultSceneMode = sceneMode;
  }

  /**
   * Set SSL related setting. This method need be invoked before
   * flashbackGlobalSetUp method, otherwise Https can't get through
   * @param rootCertificateInputStream root certificate input stream
   * @param rootCertificatePassphrase root certificate path
   * @param authority certificate authority
   */
  protected void setSslSettings(InputStream rootCertificateInputStream, String rootCertificatePassphrase,
      CertificateAuthority authority) {
    _rootCertificateInputStream = rootCertificateInputStream;
    _rootCertificatePassphrase = rootCertificatePassphrase;
    _certificateAuthority = authority;
  }

  /**
   * set the default scene name
   * (otherwise the default scene name would be null)
   * this will only affect in test class when override flashbackDefaultSetup
   * and call before super.flashbackDefaultSetup().
   * @param sceneName the default scene name
   */
  protected void setDefaultSceneName(String sceneName) {
    _defaultSceneName = sceneName;
  }

  /**
   * set the default scene path
   * (otherwise the default scene path would be '/flashback/scene' in java resource folder or home directory)
   * this will only affect in test class when override flashbackTestClassSetUp
   * and call before super.flashbackDefaultSetup().
   * @param scenePath the default scene path
   */
  protected void setDefaultScenePath(String scenePath) {
    _defaultScenePath = scenePath;
  }

  /**
   * set the default match rule
   * (otherwise the default rule would be DummyMatchRule)
   * this will only affect in test class when override flashbackTestClassSetUp
   * and call before super.flashbackDefaultSetup().
   * @param matchRule the default match rule
   */
  protected void setDefaultMatchRule(MatchRule matchRule) {
    _defaultMatchRule = matchRule;
  }

  /**
   * here is the place to be used in test case and within this callable flashback will use this matchRule.
   *
   * @param matchRule    the match rule will be loaded info flashback.
   * @param callable     to call after the match rule changed in flashback.
   * @param <T>          return type of the callable.
   * @return return callable result.
   * @throws Exception
   */
  protected <T> T withMatchRule(MatchRule matchRule, Callable<T> callable)
      throws Exception {
    try {
      _flashbackRunner.setMatchRule(matchRule);

      return (T) callable.call();
    } finally {
      _flashbackRunner.setMatchRule(_defaultMatchRule);
    }
  }

  /**
   * here is the place to be used in test case and within this callable flashback will use the scene specified by sceneName.
   *
   * @param sceneName     the scene name, will be loaded into flashback using PLAYBACK mode and default scene path.
   * @param callable     to call after the scene changed in flashback.
   * @param <T>          return type of the callable.
   * @return return callable result.
   * @throws Exception
   */
  protected <T> T withScene(String sceneName, Callable<T> callable)
      throws Exception {
    SceneConfiguration sceneConfiguration = new SceneConfiguration(_defaultScenePath, _defaultSceneMode, sceneName);
    return withScene(sceneConfiguration, callable);
  }

  /**
   * here is the place to be used in test case and within this callable flashback will use the scene specified by sceneConf.
   *
   * @param sceneConf    the scene config, will be loaded into flashback.
   * @param callable     to call after the scene changed in flashback.
   * @param <T>          return type of the callable.
   * @return return callable result.
   * @throws Exception
   */
  protected <T> T withScene(SceneConfiguration sceneConf, Callable<T> callable)
      throws Exception {
    try {
      Scene scene = SceneFactory.create(sceneConf);
      _flashbackRunner.setScene(scene);

      return (T) callable.call();
    } finally {
      _flashbackRunner.setScene(_defaultScene);
    }
  }

  /**
   * here is the place to be used in test case and within this runnable flashback will use this matchRule.
   *
   * @param matchRule    the match rule, will be loaded into flashback.
   * @param runnable     to call after the match rule loaded.
   */
  protected void withMatchRule(MatchRule matchRule, Runnable runnable) {
    try {
      _flashbackRunner.setMatchRule(matchRule);
      runnable.run();
    } finally {
      _flashbackRunner.setMatchRule(_defaultMatchRule);
    }
  }

  /**
   * here is the place to be used in test case and within this runnable flashback will use the scene according to the sceneName.
   *
   * @param sceneName     the scene name, will be loaded into flashback using PLAYBACK mode and default scene path.
   * @param runnable      to call after the scene loaded.
   */
  protected void withScene(String sceneName, Runnable runnable) {
    SceneConfiguration sceneConfiguration = new SceneConfiguration(_defaultScenePath, _defaultSceneMode, sceneName);
    withScene(sceneConfiguration, runnable);
  }

  /**
   * here is the place to be used in test case and within this runnable flashback will use the scene according to the sceneConf.
   *
   * @param sceneConf    the scene config, will be loaded into flashback.
   * @param runnable      to call after the scene loaded.
   */
  protected void withScene(SceneConfiguration sceneConf, Runnable runnable) {
    try {
      Scene scene = SceneFactory.create(sceneConf);
      _flashbackRunner.setScene(scene);
      runnable.run();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    } finally {
      _flashbackRunner.setScene(_defaultScene);
    }
  }

  protected int getProxyPort() {
    return _proxyPort;
  }

  protected String getProxyHost() {
    return _proxyHost;
  }

  protected SceneMode getDefaultSceneMode() {
    return _defaultSceneMode;
  }

  private void determineDefaultSettings()
      throws IOException {
    // if default scenePath hasn't been set, need to figure it out
    // from either your java default resource folder or home dir.
    if (_defaultScenePath == null) {
      _defaultScenePath = HOME_DIR;
      URL flashbackScene = getClass().getResource(FLASHBACK_SCENE_DIR);
      if (flashbackScene != null) {
        _defaultScenePath = flashbackScene.getPath();
      }
    }

    // if default sceneName is set, it must exist (because by default is PLAYBACK mode)
    // and we will try to load it.
    if (_defaultSceneName != null) {
      SceneConfiguration sceneConfiguration =
          new SceneConfiguration(_defaultScenePath, _defaultSceneMode, _defaultSceneName);
      _defaultScene = SceneFactory.create(sceneConfiguration);
    }

    // after determined the default Scene&MatchRule for this test class, set them.
    _flashbackRunner.setScene(_defaultScene);
    _flashbackRunner.setMatchRule(_defaultMatchRule);
  }

  private void bootstrap()
      throws InterruptedException {
    FlashbackRunner.Builder flashbackBuilder =
        new FlashbackRunner.Builder().sceneAccessLayer(new SceneAccessLayer(_defaultScene, _defaultMatchRule))
            .host(_proxyHost).port(_proxyPort).mode(_defaultSceneMode);
    if (_certificateAuthority != null && _rootCertificateInputStream != null && _rootCertificatePassphrase != null) {
      flashbackBuilder.certificateAuthority(_certificateAuthority).rootCertificateInputStream(_rootCertificateInputStream)
          .rootCertificatePassphrase(_rootCertificatePassphrase);
    }
    _flashbackRunner = flashbackBuilder.build();
    _flashbackRunner.start();
  }
}
