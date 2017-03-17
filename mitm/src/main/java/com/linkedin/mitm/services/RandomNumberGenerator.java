/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.services;

import java.security.SecureRandom;


/**
 * Singleton to generate secure random number
 *
 * @author shfeng
 */
public class RandomNumberGenerator {
  private static RandomNumberGenerator _randomNumberGenerator = new RandomNumberGenerator();
  private SecureRandom _secureRandom;

  public static RandomNumberGenerator getInstance() {
    return _randomNumberGenerator;
  }

  public SecureRandom getSecureRandom() {
    return _secureRandom;
  }

  private RandomNumberGenerator() {
    _secureRandom = new SecureRandom();
    _secureRandom.setSeed(System.currentTimeMillis());
  }
}
