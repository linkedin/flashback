/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.model;

/**
 * Hold all parameters that is used to build your own Certificate Authority
 * https://docs.oracle.com/cd/E19509-01/820-3503/ggezy/index.html
 *
 * @author shfeng
 */
public class CertificateAuthority {

  private final String _alias;
  private final char[] _passPhrase;
  private final String _commonName;
  private final String _organizationalUnit;
  private final String _organization;
  private final String _locality;
  private final String _countryCode;

  public CertificateAuthority(String alias, char[] passPhrase, String commonName, String organizationalUnit,
      String organization, String locality, String countryCode) {
    _alias = alias;
    _passPhrase = passPhrase;
    _commonName = commonName;
    _organizationalUnit = organizationalUnit;
    _organization = organization;
    _locality = locality;
    _countryCode = countryCode;
  }

  public String getAlias() {
    return _alias;
  }

  public char[] getPassPhrase() {
    return _passPhrase;
  }

  public String getCommonName() {
    return _commonName;
  }

  public String getOrganizationalUnit() {
    return _organizationalUnit;
  }

  public String getOrganization() {
    return _organization;
  }

  public String getLocality() {
    return _locality;
  }

  public String getCountryCode() {
    return _countryCode;
  }
}
