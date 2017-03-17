/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.services;

import com.linkedin.mitm.model.CertificateAuthority;
import com.linkedin.mitm.model.CertificateValidPeriod;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;


/**
 * CA certificate service that would be used to generate CA certificate, update key store entry etc.
 * ref:https://access.redhat.com/documentation/en-US/Red_Hat_Certificate_System/8.0/html/Admin_Guide/Standard_X.509_v3_Certificate_Extensions.html
 * @author shfeng
 */
public class CACertificateService extends AbstractX509CertificateService implements CertificateService {

  /**
   * The Key Usage extension defines the purpose of the key contained in the certificate
   * */
  private static final KeyUsage KEY_USAGE = new KeyUsage(
      KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment
          | KeyUsage.cRLSign);

  //The Extended Key Usage extension indicates the purposes for which the certified public key may be used.
  private static final ASN1Encodable EXTERNAL_KEY_USAGE;

  private static final BasicConstraints BASIC_CONSTRAINTS = new BasicConstraints(true);

  static {
    ASN1EncodableVector purposes = new ASN1EncodableVector();
    purposes.add(KeyPurposeId.id_kp_serverAuth);
    purposes.add(KeyPurposeId.id_kp_clientAuth);
    purposes.add(KeyPurposeId.anyExtendedKeyUsage);
    EXTERNAL_KEY_USAGE = new DERSequence(purposes);
  }

  public CACertificateService(CertificateAuthority certificateAuthority, CertificateValidPeriod certificateValidPeriod)
      throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    super(certificateAuthority, certificateValidPeriod);
  }

  @Override
  public X509Certificate createSignedCertificate(PublicKey publicKey, PrivateKey privateKey, String commonName,
      List<ASN1Encodable> sans)
      throws CertificateException, IOException, OperatorCreationException, NoSuchProviderException,
             NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    BigInteger serial = getSerial();
    X500Name subject = getSubject(commonName);
    X500Name issuer = subject;

    X509v3CertificateBuilder x509v3CertificateBuilder =
        new JcaX509v3CertificateBuilder(issuer, serial, getValidDateFrom(), getValidDateTo(), subject, publicKey);
    buildExtensions(x509v3CertificateBuilder, publicKey);
    return createCertificate(privateKey, x509v3CertificateBuilder);
  }

  protected void buildExtensions(X509v3CertificateBuilder x509v3CertificateBuilder, PublicKey publicKey)
      throws IOException {

    x509v3CertificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyIdentifier(publicKey));

    // The Key Usage, Extended Key Usage, and Basic Constraints extensions act together to define the purposes for
    // which the certificate is intended to be used
    x509v3CertificateBuilder.addExtension(Extension.basicConstraints, true, BASIC_CONSTRAINTS);

    x509v3CertificateBuilder.addExtension(Extension.keyUsage, false, KEY_USAGE);

    x509v3CertificateBuilder.addExtension(Extension.extendedKeyUsage, false, EXTERNAL_KEY_USAGE);
  }

  @Override
  public void updateKeyStore(KeyStore keyStore, PrivateKey privateKey, Certificate identityCertificate)
      throws KeyStoreException {
    updateKeyStore(keyStore, privateKey, new Certificate[]{identityCertificate});
  }
}
