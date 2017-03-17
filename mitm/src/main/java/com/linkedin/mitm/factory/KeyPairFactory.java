/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.factory;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;


/**
 * Abstract public and private key pair generating factory
 * @author shfeng
 */
public interface KeyPairFactory {
  KeyPair create()
      throws NoSuchAlgorithmException;
}
