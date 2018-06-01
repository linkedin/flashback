package com.linkedin.mitm.services;

import java.security.cert.Certificate;


public interface ServiceNameExtractor {
  String extractServiceName(Certificate certificate);
}
