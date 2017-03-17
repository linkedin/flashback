/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.services;

import com.linkedin.mitm.model.CertificateAuthority;
import com.linkedin.mitm.model.CertificateValidPeriod;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;


/**
 * Abstract X509 certificate service that implement functionality that
 * commonly used for other X509 certificate service
 *
 * @author shfeng
 */
public abstract class AbstractX509CertificateService {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private static final String SIGNATURE_ALGORITHM = "SHA1WithRSAEncryption";

  private final CertificateAuthority _certificateAuthority;
  private final CertificateValidPeriod _certificateValidPeriod;

  protected AbstractX509CertificateService(CertificateAuthority certificateAuthority,
      CertificateValidPeriod certificateValidPeriod) {
    _certificateAuthority = certificateAuthority;
    _certificateValidPeriod = certificateValidPeriod;
  }

  protected BigInteger getSerial() {
    return BigInteger.valueOf(RandomNumberGenerator.getInstance().getSecureRandom().nextLong());
  }

  /**
   * Create subjectKeyIdentifier
   * The Subject Key Identifier extension identifies the public key certified by this certificate.
   * This extension provides a way of distinguishing public keys if more than one is available for
   * a given subject name.
   * i.e.
   *     Identifier: Subject Key Identifier - 2.5.29.14
   *       Critical: no
   *        Key Identifier:
   *          3B:46:83:85:27:BC:F5:9D:8E:63:E3:BE:79:EF:AF:79:
   *          9C:37:85:84
   *
   * */
  protected SubjectKeyIdentifier createSubjectKeyIdentifier(PublicKey publicKey)
      throws IOException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(publicKey.getEncoded());
        ASN1InputStream ais = new ASN1InputStream(bais)) {
      ASN1Sequence asn1Sequence = (ASN1Sequence) ais.readObject();
      SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(asn1Sequence);
      return new BcX509ExtensionUtils().createSubjectKeyIdentifier(subjectPublicKeyInfo);
    }
  }

  protected X509Certificate createCertificate(PrivateKey privateKey, X509v3CertificateBuilder x509v3CertificateBuilder)
      throws OperatorCreationException, CertificateException {
    ContentSigner contentSigner =
        new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BouncyCastleProvider.PROVIDER_NAME)
            .build(privateKey);
    X509Certificate x509Certificate = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
        .getCertificate(x509v3CertificateBuilder.build(contentSigner));
    return x509Certificate;
  }

  protected X500Name getSubject(String commonName) {
    X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
    x500NameBuilder.addRDN(BCStyle.CN, commonName);
    x500NameBuilder.addRDN(BCStyle.O, _certificateAuthority.getOrganization());
    x500NameBuilder.addRDN(BCStyle.OU, _certificateAuthority.getOrganizationalUnit());
    return x500NameBuilder.build();
  }

  protected Date getValidDateFrom() {
    return _certificateValidPeriod.getStart();
  }

  protected Date getValidDateTo() {
    return _certificateValidPeriod.getEnd();
  }

  protected void updateKeyStore(KeyStore keystore, PrivateKey privateKey, Certificate[] certificates)
      throws KeyStoreException {
    keystore
        .setKeyEntry(_certificateAuthority.getAlias(), privateKey, _certificateAuthority.getPassPhrase(), certificates);
  }

  /**
   *  Build X.509 v3 Certificate extension
   *  @param x509v3CertificateBuilder  certificate builder
   *  @param publicKey  certificate public key
   * */
  protected abstract void buildExtensions(X509v3CertificateBuilder x509v3CertificateBuilder, PublicKey publicKey)
      throws IOException;
}
