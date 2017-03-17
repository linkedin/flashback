/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.store;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


/**
 * Read trust store from the file. This class will be used mainly for testing purpose.
 *
 * @author shfeng
 */
public class JKSTrustStoreReader implements KeyStoreReader {
  private static final String TRUST_STORE_TYPE = "JKS";

  @Override
  public KeyStore load(InputStream inputStream, String password)
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    KeyStore ksTrust = KeyStore.getInstance(TRUST_STORE_TYPE);
    try {
      ksTrust.load(inputStream, password.toCharArray());
    } finally {
      inputStream.close();
    }
    return ksTrust;
  }

  @Override
  public KeyStore load(String path, String password)
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
    return load(new FileInputStream(path), password);
  }
}