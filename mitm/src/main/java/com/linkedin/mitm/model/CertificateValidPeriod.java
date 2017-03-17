/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.model;

import java.util.Calendar;
import java.util.Date;


/**
 * Certificate signed by CA will be valid in this period
 * @author shfeng
 */
public class CertificateValidPeriod {
  private final Date _start;
  private final Date _end;

  public CertificateValidPeriod() {
    Date today = new Date();
    Calendar cal = Calendar.getInstance();
    cal.setTime(today);
    cal.add(Calendar.YEAR, -1);
    _start = cal.getTime();
    cal.setTime(today);
    cal.add(Calendar.YEAR, 10);
    _end = cal.getTime();
  }

  public CertificateValidPeriod(Date start, Date end) {
    _start = start;
    _end = end;
  }

  public Date getStart() {
    return _start;
  }

  public Date getEnd() {
    return _end;
  }
}
