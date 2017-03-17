/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.factory;

import com.linkedin.mitm.services.CertificateService;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.operator.OperatorCreationException;


/**
 *  This factory would be used to generate {@Link KeyStore} either from CA certificate
 *  or server identity certificates
 *  It will be only used to generate keyManagers not trustManagers.
 *
 * @author shfeng
 */
public class CertificateKeyStoreFactory {
  private static final String KEY_STORE_TYPE = "PKCS12";

  private final KeyPairFactory _keyPairFactory;
  private final CertificateService _certificateService;

  /**
   * @param keyPairFactory factory that would be used to generate public/private key pairs
   * @param certificateService  decide which type of certificate to create
   *
   * */
  public CertificateKeyStoreFactory(KeyPairFactory keyPairFactory, CertificateService certificateService)
      throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    _keyPairFactory = keyPairFactory;
    _certificateService = certificateService;
  }

  /**
   * create a {@link java.security.KeyStore} for this certificate
   * @param commonName  this field is only used for generating new identity certificate
   * @param sans a list of alternate subject names, that also will be used for generating identity certificate
   * @return keystore for this new certificate
   *
   * */
  public KeyStore create(String commonName, List<ASN1Encodable> sans)
      throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, OperatorCreationException,
             NoSuchProviderException, InvalidKeyException, SignatureException {
    KeyPair keyPair = _keyPairFactory.create();
    PublicKey publicKey = keyPair.getPublic();
    PrivateKey privateKey = keyPair.getPrivate();
    X509Certificate identityCertificate =
        _certificateService.createSignedCertificate(publicKey, privateKey, commonName, sans);
    KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
    keyStore.load(null, null);

    _certificateService.updateKeyStore(keyStore, privateKey, identityCertificate);
    return keyStore;
  }
}
