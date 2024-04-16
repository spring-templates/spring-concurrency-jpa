package com.concurrency.thread.counter.impl;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * AtomicCounter class is an implementation of Counter interface. It uses
 * AtomicInteger to store the counter value.
 */
@Component
public final class AtomicCounter implements Counter {

  /**
   * The counter value.
   */
  private final AtomicInteger count = new AtomicInteger(100);

  @Override
  public void add(final int value) {
    count.addAndGet(value);
  }

  @Override
  public int show() {
    return count.get();
  }

  @Override
  public void clear() {
    count.set(0);
  }
}
