/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.services;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.operator.OperatorCreationException;


/**
 * Interface of certificate service
 * @author shfeng
 */
public interface CertificateService {

  /**
   * create signed certificate using various parameters
   *
   * @param publicKey  Certificate public key
   * @param privateKey Certificate private key
   * @param commonName  For CA certificate, it comes from Certificate Authority For server identity common name. it's from server certificate.
   * @param sans  a list of subject alternate name. It's not needed for generating CA certificate
   * @return signed certificate either by itself(CA certificate) or issuer(CA)
   * */
  public X509Certificate createSignedCertificate(PublicKey publicKey, PrivateKey privateKey, String commonName,
      List<ASN1Encodable> sans)
      throws CertificateException, IOException, OperatorCreationException, NoSuchProviderException,
             NoSuchAlgorithmException, InvalidKeyException, SignatureException;

  /**
   *  update key store entry
   *  @param keyStore to be updated
   *  @param privateKey private key of this identity certificate
   *  @param identityCertificate
   *  */
  public void updateKeyStore(KeyStore keyStore, PrivateKey privateKey, Certificate identityCertificate)
      throws KeyStoreException;
}
