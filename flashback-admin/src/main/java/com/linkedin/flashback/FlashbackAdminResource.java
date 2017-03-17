/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback;

import com.linkedin.flashback.factory.SceneFactory;
import com.linkedin.flashback.matchrules.MatchRule;
import com.linkedin.flashback.matchrules.NamedMatchRule;
import com.linkedin.flashback.scene.SceneConfiguration;
import com.linkedin.flashback.scene.SceneMode;
import com.linkedin.flashback.smartproxy.FlashbackRunner;
import com.linkedin.mitm.model.CertificateAuthority;
import com.linkedin.restli.common.HttpStatus;
import com.linkedin.restli.server.RestLiServiceException;
import com.linkedin.restli.server.annotations.Action;
import com.linkedin.restli.server.annotations.ActionParam;
import com.linkedin.restli.server.annotations.Optional;
import com.linkedin.restli.server.annotations.RestLiActions;
import java.io.IOException;

/**
 * This Rest.li resource exposed API to control FlashbackRunner.
 *
 * @author shfeng
 */
@RestLiActions(name = "admin", namespace = "com.linkedin.flashback")
public class FlashbackAdminResource{
  private static FlashbackRunner _flashbackRunner;
  private static SceneMode _currSceneMode;
  private static String _scenePath;

  @Action(name = "startFlashback")
  public void startFlashback(@ActionParam("sceneMode") String sceneMode,
      @ActionParam("sceneName") String sceneName,
      @ActionParam("matchRule") String matchRule,
      @ActionParam("scenePath") String scenePath,
      @ActionParam("proxyHost") @Optional String proxyHost,
      @ActionParam("proxyPort") @Optional String proxyPort,
      @ActionParam("caCertPath") @Optional String caCertPath,
      @ActionParam("caCertPwd") @Optional String caCertPwd,
      @ActionParam("caAlias") @Optional String caAlias,
      @ActionParam("caKeyPwd") @Optional String caKeyPwd,
      @ActionParam("caCertCN") @Optional String caCertCN,
      @ActionParam("caCertOU") @Optional String caCertOU,
      @ActionParam("caCertO") @Optional String caCertO,
      @ActionParam("caCertL") @Optional String caCertL,
      @ActionParam("caCertCC") @Optional String caCertCC) {
    _currSceneMode = SceneMode.fromString(sceneMode);
    if (_currSceneMode == null) {
      throw new RestLiServiceException(HttpStatus.S_400_BAD_REQUEST, "Unknown scene mode" + sceneMode);
    }
    MatchRule namedMatchRule = NamedMatchRule.fromString(matchRule);
    if (namedMatchRule == null) {
      throw new RestLiServiceException(HttpStatus.S_400_BAD_REQUEST, "Unknown match rule" + matchRule);
    }
    _scenePath = scenePath;
    SceneConfiguration sceneConfiguration = new SceneConfiguration(_scenePath, _currSceneMode, sceneName);
    try {
      FlashbackRunner.Builder builder = new FlashbackRunner.Builder().mode(_currSceneMode)
          .sceneAccessLayer(new SceneAccessLayer(SceneFactory.create(sceneConfiguration), namedMatchRule));
      if (proxyHost != null) {
        builder.host(proxyHost);
      }
      if (proxyPort != null) {
        builder.port(Integer.parseInt(proxyPort));
      }
      // Setup SSL setting. Those proprties are not mandatory for flashback but mandatory if it requires flashback
      // to record/replay HTTPS conversation.
      if (caAlias != null && caCertPwd != null && caCertPath != null && caKeyPwd != null && caCertOU != null
          && caCertO != null) {
        builder.rootCertificatePath(caCertPath)
            .rootCertificatePassphrase(caCertPwd)
            .certificateAuthority(
                new CertificateAuthority(caAlias, caKeyPwd.toCharArray(), caCertCN, caCertOU, caCertO, caCertL,
                    caCertCC));
      }
      _flashbackRunner = builder.build();
      _flashbackRunner.start();
    } catch (IOException | InterruptedException e) {
      throw new RestLiServiceException(HttpStatus.S_500_INTERNAL_SERVER_ERROR, e);
    }
  }

  @Action(name = "changeScene")
  public void changeScene(@ActionParam("sceneName") String sceneName) {
    validate();
    SceneConfiguration sceneConfiguration = new SceneConfiguration(_scenePath, _currSceneMode, sceneName);
    try {
      _flashbackRunner.setScene(SceneFactory.create(sceneConfiguration));
    } catch (IOException e) {
      throw new RestLiServiceException(HttpStatus.S_500_INTERNAL_SERVER_ERROR, e);
    }
  }

  @Action(name = "changeMatchRule")
  public void changeMatchRule(@ActionParam("matchRule") String matchRule) {
    validate();
    MatchRule namedMatchRule = NamedMatchRule.fromString(matchRule);
    if (namedMatchRule == null) {
      throw new RestLiServiceException(HttpStatus.S_400_BAD_REQUEST, "Unknown match rule" + matchRule);
    }
    _flashbackRunner.setMatchRule(namedMatchRule);
  }

  @Action(name = "shutDownFlashback")
  public void shutDownFlashback() {
    validate();
    _flashbackRunner.stop();
  }

  private void validate() {
    if (_flashbackRunner == null) {
      throw new RestLiServiceException(HttpStatus.S_400_BAD_REQUEST, "FlashbackRunner is not started ");
    }
  }
}
