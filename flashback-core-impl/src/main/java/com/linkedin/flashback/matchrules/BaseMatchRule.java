/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.matchrules;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


/**
 *  Abstract base {@link com.linkedin.flashback.matchrules.MatchRule} class
 * This class is required because derived class will be used as lookup key in
 * {@link CompositeMatchRule}
 *
 *  @author shfeng
 */
public abstract class BaseMatchRule implements MatchRule {
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
