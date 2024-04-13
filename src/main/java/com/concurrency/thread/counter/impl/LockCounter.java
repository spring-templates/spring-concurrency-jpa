package com.concurrency.thread.counter.impl;

import com.concurrency.thread.counter.executor.CounterConfiguration;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public final class LockCounter implements Counter {

  /**
   * The lock.
   */
  private static final ReentrantLock LOCK = new ReentrantLock();
  /**
   * The counter value.
   */
  private static int count = CounterConfiguration.DEFAULT_VALUE;

  @Override
  public void add(final int value) {
    LOCK.lock();
    try {
      count += value;
    } finally {
      LOCK.unlock();
    }
  }

  @Override
  public int show() {
    return count;
  }

  @Override
  public void clear() {
    LOCK.lock();
    try {
      count = 0;
    } finally {
      LOCK.unlock();
    }
  }
}
