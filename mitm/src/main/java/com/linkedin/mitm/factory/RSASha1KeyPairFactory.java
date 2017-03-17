/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.factory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * Generate public key and private key pair that use RSA and SHA1PRNG
 *
 * @author shfeng
 */
public class RSASha1KeyPairFactory implements KeyPairFactory {
  private static final String KEYGEN_ALGORITHM = "RSA";
  private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

  private final int _keySize;

  public RSASha1KeyPairFactory(int keySize) {
    _keySize = keySize;
  }

  @Override
  public KeyPair create()
      throws NoSuchAlgorithmException {
    KeyPairGenerator generator = KeyPairGenerator.getInstance(KEYGEN_ALGORITHM);
    SecureRandom secureRandom = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
    generator.initialize(_keySize, secureRandom);
    return generator.generateKeyPair();
  }
}
