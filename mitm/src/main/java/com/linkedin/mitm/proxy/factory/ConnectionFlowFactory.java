/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.factory;

import com.linkedin.mitm.factory.CertificateKeyStoreFactory;
import com.linkedin.mitm.factory.RSASha1KeyPairFactory;
import com.linkedin.mitm.model.CertificateAuthority;
import com.linkedin.mitm.model.CertificateValidPeriod;
import com.linkedin.mitm.proxy.connectionflow.steps.AcceptTCPConnectionFromClient;
import com.linkedin.mitm.proxy.connectionflow.steps.ConnectionFlowStep;
import com.linkedin.mitm.proxy.connectionflow.steps.EstablishTCPConnectionToServer;
import com.linkedin.mitm.proxy.connectionflow.steps.HandshakeWithClient;
import com.linkedin.mitm.proxy.connectionflow.steps.HandshakeWithServer;
import com.linkedin.mitm.proxy.connectionflow.steps.ResumeReadingFromClient;
import com.linkedin.mitm.proxy.connectionflow.steps.StopReadingFromClient;
import com.linkedin.mitm.services.IdentityCertificateService;
import com.linkedin.mitm.services.SSLContextGenerator;
import com.linkedin.mitm.store.PKC12KeyStoreReadWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SSLContext;


/**
 * Connection flow factory that implemented a few connection flows for Http
 * and Https.
 *
 * The connection flow will be executed by MITM proxy in the order you defined in factory
 * The Https connection flow here assume we can extract host name from Http CONNECT request.
 *
 * @author shfeng
 */
public class ConnectionFlowFactory {
  /**
   * Create Https connection flow including client to proxy connection and proxy to server connection
   *
   * @param rootCertificateInputStream input stream of root certificate
   * @param rootCertificatePassphrase  pass phrase of this root certificate
   * @param certificateAuthority description of certificate authority
   * @return Connection flow
   * */
  public static List<ConnectionFlowStep> createFullHttpsConnectionFlow(InputStream rootCertificateInputStream,
      String rootCertificatePassphrase, CertificateAuthority certificateAuthority) {
    try {
      PKC12KeyStoreReadWriter pkc12KeyStoreReadWriter = new PKC12KeyStoreReadWriter();
      KeyStore issuerKeyStore = pkc12KeyStoreReadWriter.load(rootCertificateInputStream, rootCertificatePassphrase);
      SSLContext clientSslContext =
          SSLContextGenerator.createClientContext(issuerKeyStore, certificateAuthority.getPassPhrase());
      CertificateValidPeriod defaultPeriod = new CertificateValidPeriod();
      CertificateKeyStoreFactory certificateKeyStoreFactory =
          new CertificateKeyStoreFactory(new RSASha1KeyPairFactory(1024),
              new IdentityCertificateService(certificateAuthority, defaultPeriod, issuerKeyStore));
      List<ConnectionFlowStep> connectionFlowSteps = new ArrayList<>();
      connectionFlowSteps.add(new StopReadingFromClient());
      connectionFlowSteps.add(new EstablishTCPConnectionToServer());
      connectionFlowSteps.add(new HandshakeWithServer(clientSslContext));
      connectionFlowSteps.add(new AcceptTCPConnectionFromClient());
      connectionFlowSteps.add(new ResumeReadingFromClient());
      connectionFlowSteps.add(new HandshakeWithClient(certificateKeyStoreFactory, certificateAuthority));
      return Collections.unmodifiableList(connectionFlowSteps);
    } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | UnrecoverableKeyException | KeyManagementException e) {
      throw new RuntimeException("Failed to load root certificate from input stream", e);
    }
  }

  /**
   * Create Http connection flow including client to proxy connection and proxy to server connection
   */
  public static List<ConnectionFlowStep> createFullHttpConnectionFlow() {
    List<ConnectionFlowStep> connectionFlowSteps = new ArrayList<>();
    connectionFlowSteps.add(new StopReadingFromClient());
    connectionFlowSteps.add(new EstablishTCPConnectionToServer());
    connectionFlowSteps.add(new ResumeReadingFromClient());
    return Collections.unmodifiableList(connectionFlowSteps);
  }

  /**
   * Create Https connection flow including client to proxy connection only
   *
   * @param rootCertificateInputStream input stream of root certificate
   * @param rootCertificatePassphrase  pass phrase of this root certificate
   * @param certificateAuthority description of certificate authority
   * @return Connection flow
   * */
  public static List<ConnectionFlowStep> createClientOnlyHttpsConnectionFlow(InputStream rootCertificateInputStream,
      String rootCertificatePassphrase, CertificateAuthority certificateAuthority) {
    try {
      PKC12KeyStoreReadWriter pkc12KeyStoreReadWriter = new PKC12KeyStoreReadWriter();
      KeyStore issuerKeyStore = pkc12KeyStoreReadWriter.load(rootCertificateInputStream, rootCertificatePassphrase);
      CertificateValidPeriod defaultPeriod = new CertificateValidPeriod();
      CertificateKeyStoreFactory certificateKeyStoreFactory =
          new CertificateKeyStoreFactory(new RSASha1KeyPairFactory(1024),
              new IdentityCertificateService(certificateAuthority, defaultPeriod, issuerKeyStore));
      List<ConnectionFlowStep> connectionFlowSteps = new ArrayList<>();
      connectionFlowSteps.add(new StopReadingFromClient());
      connectionFlowSteps.add(new AcceptTCPConnectionFromClient());
      connectionFlowSteps.add(new ResumeReadingFromClient());
      connectionFlowSteps.add(new HandshakeWithClient(certificateKeyStoreFactory, certificateAuthority));
      return Collections.unmodifiableList(connectionFlowSteps);
    } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | IOException | UnrecoverableKeyException e) {
      throw new RuntimeException("Failed to load root certificate from input stream", e);
    }
  }

  /**
   * Create Http connection flow including client to proxy connection only.
   * There is no connection flow step required for this case so it's empty
   */
  public static List<ConnectionFlowStep> createClientOnlyHttpConnectionFlow() {
    return Collections.emptyList();
  }

  private ConnectionFlowFactory() {

  }
}
