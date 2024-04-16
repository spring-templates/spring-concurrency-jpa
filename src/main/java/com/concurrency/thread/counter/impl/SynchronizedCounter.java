package com.concurrency.thread.counter.impl;

import com.concurrency.thread.counter.executor.CounterConfiguration;
import org.springframework.stereotype.Component;

@Component
public final class SynchronizedCounter implements Counter {

  /**
   * The counter value.
   */
  private int counter = CounterConfiguration.DEFAULT_VALUE;

  @Override
  public synchronized void add(final int value) {
    counter += value;
  }

  @Override
  public synchronized int show() {
    return counter;
  }

  @Override
  public synchronized void clear() {
    counter = 0;
  }
}
