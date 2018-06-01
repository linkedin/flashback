package com.linkedin.mitm.services;

import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.TrustManagerFactory;


/**
 * Helper class for creating a TrustManagerFactory
 */
public class TrustManagerFactoryGenerator {

  public static TrustManagerFactory newTrustManagerFactory(String trustStorePath, char[] trustStorePwd, String keyStoreType) throws
                                                                                                                             Exception {
    try (FileInputStream fileInputStream = new FileInputStream(trustStorePath)) {
      KeyStore ts = KeyStore.getInstance(keyStoreType);
      ts.load(fileInputStream, trustStorePwd);

      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ts);
      return tmf;
    }
  }
}
