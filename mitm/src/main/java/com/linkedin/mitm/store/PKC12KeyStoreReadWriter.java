/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.store;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


/**
 * @author shfeng
 */
public class PKC12KeyStoreReadWriter implements KeyStoreReader, KeyStoreWriter {
  /**
   * The P12 format has to be implemented by every vendor. Oracles proprietary
   * JKS type is not available in Android.
   */
  private static final String KEY_STORE_TYPE = "PKCS12";

  @Override
  public KeyStore load(InputStream inputstream, String password)
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    KeyStore ksKeys = KeyStore.getInstance(KEY_STORE_TYPE);
    try {
      ksKeys.load(inputstream, password.toCharArray());
    } finally {
      inputstream.close();
    }
    return ksKeys;
  }

  @Override
  public KeyStore load(String path, String password)
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    return load(new FileInputStream(path), password);
  }

  @Override
  public void store(String filePath, KeyStore keyStore, String passphrase)
      throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
    try (OutputStream fos = new FileOutputStream(filePath)) {
      keyStore.store(fos, passphrase.toCharArray());
    }
  }
}
