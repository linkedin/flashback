/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.store;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


/**
 * Interface of Key store writer
 * @author shfeng
 */
public interface KeyStoreWriter {
  void store(String filePath, KeyStore keyStore, String passphrase)
      throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException;
}
