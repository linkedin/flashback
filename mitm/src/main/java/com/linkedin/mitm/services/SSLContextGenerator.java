/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.services;

import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
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
  private static final String KEY_MANAGER_TYPE = "SunX509";
  private static final String TRUST_MANAGER_TYPE = "SunX509";

  /**
   * Create client side SSLContext {@link javax.net.ssl.SSLContext}
   *
   * */
  public static SSLContext createClientContext(KeyStore keyStore, char[] passphrase)
      throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
    String keyManAlg = KeyManagerFactory.getDefaultAlgorithm();
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManAlg);
    kmf.init(keyStore, passphrase);
    KeyManager[] keyManagers = kmf.getKeyManagers();
    return create(keyManagers, InsecureTrustManagerFactory.INSTANCE.getTrustManagers(),
        RandomNumberGenerator.getInstance().getSecureRandom());
  }

  /**
   * Create default server side SSLContext {@link javax.net.ssl.SSLContext}
   *
   * */
  public static SSLContext createDefaultServerContext()
      throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
    return create(null, null, RandomNumberGenerator.getInstance().getSecureRandom());
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
