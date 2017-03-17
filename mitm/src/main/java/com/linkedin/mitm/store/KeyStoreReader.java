/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.store;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


/**
 * Interface of Key store reader
 * @author shfeng
 */
public interface KeyStoreReader {
  KeyStore load(InputStream inputStream, String password)
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException;

  KeyStore load(String path, String password)
      throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException;
}
