/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.services;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


/**
 * Wrapper class for SSLContext generating code
 * @author shfeng
 */
public class SSLContextGenerator {
  private static final String SSL_CONTEXT_PROTOCOL = "TLS";
  private static final String TRUST_MANAGER_TYPE = "SunX509";
  private static final String KEY_STORE_TYPE = "JKS";
  private static final String CA_STORE = "/etc/riddler/cacerts";
  private static final String CA_PASSWORD = "changeit";

  /**
   * Create SSLContext for ssl traffic between client and proxy {@link javax.net.ssl.SSLContext}
   **/
  public static SSLContext createClientContext(KeyStore keyStore, char[] passphrase) throws Exception {
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
    trustManagerFactory.init(keyStore);
    String keyManAlg = KeyManagerFactory.getDefaultAlgorithm();
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManAlg);
    kmf.init(keyStore, passphrase);
    KeyManager[] keyManagers = kmf.getKeyManagers();

    // set up trust manager factory to use riddler trust store
    TrustManagerFactory
        tmf = TrustManagerFactoryGenerator.newTrustManagerFactory(CA_STORE, CA_PASSWORD.toCharArray(), KEY_STORE_TYPE);

    TrustManager[] trustManagers = tmf.getTrustManagers();

    return create(keyManagers, trustManagers,
        RandomNumberGenerator.getInstance().getSecureRandom());
  }

  /**
   * Create SSLContext for ssl traffic between proxy and destination server {@link javax.net.ssl.SSLContext}
   **/
  public static SSLContext createDefaultServerContext() throws KeyManagementException, NoSuchAlgorithmException {
    return create(null, null, RandomNumberGenerator.getInstance().getSecureRandom());
  }

  /**
   * Create SSLContext for ssl traffic between proxy and destination server {@link javax.net.ssl.SSLContext}
   **/
  public static SSLContext createCustomServerContext(InputStream inputStream, String password)
      throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    KeyStore keyStore = KeyStore.getInstance("JKS");

    // load default Riddler keystore
    try {
      keyStore.load(inputStream, password.toCharArray());

      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(keyStore);

      return create(null, tmf.getTrustManagers(), RandomNumberGenerator.getInstance().getSecureRandom());
    } catch (IOException | CertificateException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static SSLContext create(KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom)
      throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
    sslContext.init(keyManagers, trustManagers, secureRandom);
    return sslContext;
  }

  private SSLContextGenerator() {
  }
}
