/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.connectionflow.steps;

import com.linkedin.mitm.factory.CertificateKeyStoreFactory;
import com.linkedin.mitm.model.CertificateAuthority;
import com.linkedin.mitm.proxy.channel.ChannelMediator;
import com.linkedin.mitm.services.SSLContextGenerator;
import io.netty.util.concurrent.Future;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;
import org.apache.log4j.Logger;
import org.bouncycastle.operator.OperatorCreationException;


/**
 * Accept client handshaking and complete handshaking using dynamically generated certificate.
 *
 * @author shfeng
 */
public class HandshakeWithClient implements ConnectionFlowStep {
  private static final String MODULE = HandshakeWithClient.class.getName();
  private static final Logger LOG = Logger.getLogger(MODULE);
  private final CertificateKeyStoreFactory _certificateKeyStoreFactory;
  private final CertificateAuthority _certificateAuthority;

  private final ConcurrentHashMap<String, SSLContext> _sslContextCache = new ConcurrentHashMap<>();

  public HandshakeWithClient(CertificateKeyStoreFactory certificateKeyStoreFactory,
      CertificateAuthority certificateAuthority) {
    _certificateKeyStoreFactory = certificateKeyStoreFactory;
    _certificateAuthority = certificateAuthority;
  }

  @Override
  public Future execute(ChannelMediator channelMediator, InetSocketAddress remoteAddress) {
    //dynamically create SSLEngine based on CN and SANs
    LOG.debug("Starting client connection handshaking");

    //TODO: if connect request only contains ip address, we need get either CA
    //TODO: or SANS from server response
    String remoteHost = remoteAddress.getHostName();
    SSLContext sslContext = _sslContextCache.computeIfAbsent(remoteHost, key -> {
      try {
        KeyStore keyStore = _certificateKeyStoreFactory.create(remoteHost, new ArrayList<>());
        return SSLContextGenerator.createClientContext(keyStore, _certificateAuthority.getPassPhrase());
      } catch (Exception e) {
        throw new RuntimeException("Failed to create server identity certificate for " + remoteHost, e);
      }
    });
    return channelMediator.handshakeWithClient(sslContext.createSSLEngine());
  }

}
