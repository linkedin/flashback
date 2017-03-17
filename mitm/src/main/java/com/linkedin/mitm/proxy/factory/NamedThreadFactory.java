/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.mitm.proxy.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A ThreadFactory that produces threads that include the given name.
 *
 * @author shfeng
 */
public class NamedThreadFactory implements ThreadFactory {
  private final String _namePrefix;
  private AtomicInteger _threadCount = new AtomicInteger(0);

  public NamedThreadFactory(String namePrefix) {
    _namePrefix = namePrefix;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    return new Thread(runnable, String.format("%s-%d", _namePrefix, _threadCount.getAndIncrement()));
  }
}
