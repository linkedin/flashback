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
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;


/**
 * This class is used for generating server side certificate based on
 * CA certificate, CN and SAN
 * @author shfeng
 */
public class IdentityCertificateService extends AbstractX509CertificateService implements CertificateService {

  private static final BasicConstraints BASIC_CONSTRAINTS = new BasicConstraints(false);
  private final PrivateKey _issuerPrivateKey;
  private final X509Certificate _issuerCertificate;

  /**
   * @param certificateAuthority factory to generate public key/ private key pair
   * @param issuerKeyStore this filed could be null if we are building root certificate that issued by nobody
   *
   * */
  public IdentityCertificateService(CertificateAuthority certificateAuthority,
      CertificateValidPeriod certificateValidPeriod, KeyStore issuerKeyStore)
      throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
    super(certificateAuthority, certificateValidPeriod);
    _issuerPrivateKey =
        (PrivateKey) issuerKeyStore.getKey(certificateAuthority.getAlias(), certificateAuthority.getPassPhrase());
    _issuerCertificate = (X509Certificate) issuerKeyStore.getCertificate(certificateAuthority.getAlias());
  }

  /**
   * Create a certificate using key pair and signing certificate with CA certificate, common name and a list of subjective alternate name
   *
   * @return signed sever identity certificate
   * */
  @Override
  public X509Certificate createSignedCertificate(PublicKey publicKey, PrivateKey privateKey, String commonName,
      List<ASN1Encodable> sans)
      throws CertificateException, IOException, OperatorCreationException, NoSuchProviderException,
             NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    X500Name issuer = new X509CertificateHolder(_issuerCertificate.getEncoded()).getSubject();
    BigInteger serial = getSerial();
    X500Name subject = getSubject(commonName);

    X509v3CertificateBuilder x509v3CertificateBuilder =
        new JcaX509v3CertificateBuilder(issuer, serial, getValidDateFrom(), getValidDateTo(), subject, publicKey);
    buildExtensions(x509v3CertificateBuilder, publicKey);

    fillSans(sans, x509v3CertificateBuilder);

    X509Certificate signedCertificate = createCertificate(_issuerPrivateKey, x509v3CertificateBuilder);

    signedCertificate.checkValidity();
    signedCertificate.verify(_issuerCertificate.getPublicKey());

    return signedCertificate;
  }

  @Override
  public void updateKeyStore(KeyStore keyStore, PrivateKey privateKey, Certificate identityCertificate)
      throws KeyStoreException {
    updateKeyStore(keyStore, privateKey, new Certificate[]{identityCertificate, _issuerCertificate});
  }

  /**
   * Fill subject alternate names in to signedCertificatebuilder to build new certificate
   * @param sans  a list of subject alternate name.
   *
   * */
  private void fillSans(List<ASN1Encodable> sans, X509v3CertificateBuilder x509v3CertificateBuilder)
      throws CertIOException {
    if (!sans.isEmpty()) {
      ASN1Encodable[] encodables = sans.toArray(new ASN1Encodable[sans.size()]);
      x509v3CertificateBuilder.addExtension(Extension.subjectAlternativeName, false, new DERSequence(encodables));
    }
  }

  @Override
  protected void buildExtensions(X509v3CertificateBuilder x509v3CertificateBuilder, PublicKey publicKey)
      throws IOException {
    x509v3CertificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyIdentifier(publicKey));
    x509v3CertificateBuilder.addExtension(Extension.basicConstraints, false, BASIC_CONSTRAINTS);
  }
}
