/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.smartproxy;

import com.linkedin.flashback.SceneAccessLayer;
import com.linkedin.flashback.matchrules.MatchRule;
import com.linkedin.flashback.scene.Scene;
import com.linkedin.flashback.scene.SceneMode;
import com.linkedin.flashback.smartproxy.proxycontroller.RecordController;
import com.linkedin.flashback.smartproxy.proxycontroller.ReplayController;
import com.linkedin.mitm.model.CertificateAuthority;
import com.linkedin.mitm.model.Protocol;
import com.linkedin.mitm.proxy.ProxyServer;
import com.linkedin.mitm.proxy.connectionflow.steps.ConnectionFlowStep;
import com.linkedin.mitm.proxy.dataflow.ProxyModeController;
import com.linkedin.mitm.proxy.dataflow.ProxyModeControllerFactory;
import com.linkedin.mitm.proxy.factory.ConnectionFlowFactory;
import io.netty.handler.codec.http.HttpRequest;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import org.apache.log4j.Logger;


/**
 * Flashback runner which bootstrap based on configs we pass in.
 * It also allows to change scene and match rules dynamically.
 * Note: this class is not thread safe.
 *
 * i.e.
 * How to record:
 * <pre>
 *  {@code
 *    SceneConfiguration sceneConfiguration = new SceneConfiguration("/root", SceneMode.RECORD,"file name");
 *    try (FlashbackRunner flashbackRunner = new FlashbackRunner.Builder()
 *                                      .rootCertificatePath("gaap.p12").rootCertificateFileInputStream(fis).rootCertificatePassphrase("changeit")
 *                                      .certificateAuthority(certificateAuthority)
 *                                      .mode(SceneMode.RECORD)
 *                                      .sceneAccessLayer(new SceneAccessLayer(SceneFactory.create(sceneConfiguration), MatchRuleUtils.matchEntireRequest()))
 *                                      .build()) {
 *      flashbackRunner.start();
 *      String url = "https://www.example.org/";
 *      HttpHost host = new HttpHost("localhost", 5555);
 *      HttpClient client = HttpClientBuilder.create().setProxy(host).build();
 *      HttpResponse httpResponse = client.execute(request);
 *    }
 *  }
 * </pre>
 *
 * How to replay:
 * <pre>
 *  {@code
 *    SceneConfiguration sceneConfiguration = new SceneConfiguration("/root", SceneMode.PLAYBACK,"file name");
 *    try (FlashbackRunner flashbackRunner = new FlashbackRunner.Builder()
 *                                      .rootCertificatePath("gaap.p12").rootCertificatePassphrase("changeit")
 *                                      .certificateAuthority(certificateAuthority)
 *                                      .mode(SceneMode.PLAYBACK)
 *                                      .sceneAccessLayer(new SceneAccessLayer(SceneFactory.create(sceneConfiguration), MatchRuleUtils.matchEntireRequest()))
 *                                      .build()) {
 *      flashbackRunner.start();
 *      String url = "https://www.example.org/";
 *      HttpHost host = new HttpHost("localhost", 5555);
 *      HttpClient client = HttpClientBuilder.create().setProxy(host).build();
 *
 *      //Change Scene
 *      flashbackRunner.setScene(SceneFactory.create(new SceneConfiguration("/root", SceneMode.PLAYBACK,"new file name")));
 *      url = "https://www.google.com/";
 *      request = new HttpGet(url);
 *      HttpResponse httpResponse = client.execute(request);
 *    }
 *  }
 * </pre>
 *
 * @author shfeng
 *
 */
public class FlashbackRunner implements AutoCloseable {
  private static final String MODULE = FlashbackRunner.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private final ProxyServer _proxyServer;
  private final SceneAccessLayer _sceneAccessLayer;
  private boolean _running;

  private FlashbackRunner(final Builder builder) {
    _sceneAccessLayer = builder._sceneAccessLayer;
    if (builder._sceneMode == SceneMode.RECORD || builder._sceneMode == SceneMode.SEQUENTIAL_RECORD) {
      _proxyServer = createProxyServerInRecordMode(builder);
    } else {
      _proxyServer = createProxyServerInReplayMode(builder);
    }
  }

  public void start()
      throws InterruptedException {
    _proxyServer.start();
    _running = true;
  }

  public void stop() {
    if (!_running) {
      throw new IllegalStateException("Flashback proxy server is already stopped");
    }
    _sceneAccessLayer.flush();
    _proxyServer.stop();
    _running = false;
  }

  public void setScene(Scene scene) {
    _sceneAccessLayer.flush();
    _sceneAccessLayer.setScene(scene);
  }

  public void setMatchRule(MatchRule matchRule) {
    _sceneAccessLayer.setMatchRule(matchRule);
  }

  @Override
  public void close() {
    if (_running) {
      stop();
    }
  }

  /**
   * Create proxy server in replay mode
   */
  private ProxyServer createProxyServerInReplayMode(Builder builder) {
    ProxyModeControllerFactory proxyModeControllerFactory = new ProxyModeControllerFactory() {
      @Override
      public ProxyModeController create(HttpRequest httpRequest) {
        return new ReplayController(_sceneAccessLayer, httpRequest);
      }
    };

    //Create Http connection flow for replay mode
    List<ConnectionFlowStep> httpReplayConnectionFlow = ConnectionFlowFactory.createClientOnlyHttpConnectionFlow();
    ProxyServer.Builder proxyServerBuilder =
        new ProxyServer.Builder().proxyModeControllerFactory(proxyModeControllerFactory)
            .connectionFlow(Protocol.HTTP, httpReplayConnectionFlow).host(builder._host).port(builder._port);
    if (requiresHttps(builder)) {
      //Create Https connection flow for replay mode
      List<ConnectionFlowStep> httpsReplayConnectionFlow = ConnectionFlowFactory
          .createClientOnlyHttpsConnectionFlow(builder._rootCertificateInputStream, builder._rootCertificatePassphrase,
              builder._certificateAuthority);
      proxyServerBuilder.connectionFlow(Protocol.HTTPS, httpsReplayConnectionFlow);
    }
    return proxyServerBuilder.build();
  }

  /**
   * Create proxy server in record mode
   */
  private ProxyServer createProxyServerInRecordMode(Builder builder) {
    ProxyModeControllerFactory proxyModeControllerFactory = new ProxyModeControllerFactory() {
      @Override
      public ProxyModeController create(HttpRequest httpRequest) {
        return new RecordController(_sceneAccessLayer, httpRequest);
      }
    };
    //Create Http connection flow for record mode
    List<ConnectionFlowStep> httpConnectionFlow = ConnectionFlowFactory.createFullHttpConnectionFlow();
    ProxyServer.Builder proxyServerBuilder =
        new ProxyServer.Builder().proxyModeControllerFactory(proxyModeControllerFactory)
            .connectionFlow(Protocol.HTTP, httpConnectionFlow).host(builder._host).port(builder._port);
    if (requiresHttps(builder)) {
      //Create Https connection flow for record mode
      List<ConnectionFlowStep> httpsConnectionFlow = ConnectionFlowFactory
          .createFullHttpsConnectionFlow(builder._rootCertificateInputStream, builder._rootCertificatePassphrase,
              builder._certificateAuthority);
      proxyServerBuilder.connectionFlow(Protocol.HTTPS, httpsConnectionFlow);
    }
    return proxyServerBuilder.build();
  }

  private boolean requiresHttps(Builder builder) {
    if (builder._rootCertificateInputStream == null || builder._rootCertificatePassphrase == null
        || builder._certificateAuthority == null) {
      LOG.warn("With current setup, it can't intercept HTTPS request.");
      return false;
    }
    return true;
  }

  public static class Builder {
    private String _host = "127.0.0.1";
    private int _port = 5555;
    private SceneMode _sceneMode = SceneMode.PLAYBACK;
    private InputStream _rootCertificateInputStream;
    private String _rootCertificatePassphrase;
    private CertificateAuthority _certificateAuthority;
    private SceneAccessLayer _sceneAccessLayer;

    /**
     * @param port proxy port number
     *        Default: 5555
     */
    public Builder port(int port) {
      _port = port;
      return this;
    }

    /**
     * @param host proxy host address
     *        Default: localhost
     */
    public Builder host(String host) {
      _host = host;
      return this;
    }

    /**
     * @param sceneMode scene mode
     *        Default: Playback
     */
    public Builder mode(SceneMode sceneMode) {
      _sceneMode = sceneMode;
      return this;
    }

    /**
     * @param path root certificate path.
     *             It's only required for Https
     */
    public Builder rootCertificatePath(String path)
        throws FileNotFoundException {
      _rootCertificateInputStream = new FileInputStream(path);
      return this;
    }

    /**
     *
     */
    public Builder rootCertificateInputStream(InputStream inputstream) {
      _rootCertificateInputStream = inputstream;
      return this;
    }

    /**
     * @param rootCertificatePassphrase root certificate passsphrase
     *                                  It's only required for Https
     */
    public Builder rootCertificatePassphrase(String rootCertificatePassphrase) {
      _rootCertificatePassphrase = rootCertificatePassphrase;
      return this;
    }

    /**
     * @param certificateAuthority Certificate authority information, which is required to generate server certificates
     *                             It's only required for Https
     */
    public Builder certificateAuthority(CertificateAuthority certificateAuthority) {
      _certificateAuthority = certificateAuthority;
      return this;
    }

    /**
     * @param sceneAccessLayer Access layer to record/replay scenes.
     */
    public Builder sceneAccessLayer(SceneAccessLayer sceneAccessLayer) {
      _sceneAccessLayer = sceneAccessLayer;
      return this;
    }

    public FlashbackRunner build() {
      validate();
      return new FlashbackRunner(this);
    }

    private void validate() {
      if (_sceneAccessLayer == null) {
        throw new IllegalStateException("scene access layer can't be null");
      }
    }
  }
}
