package com.concurrency.thread.counter.impl;

import com.concurrency.thread.counter.executor.CounterConfiguration;
import org.springframework.stereotype.Component;

@Component
public final class PollingCounter implements Counter {

  /**
   * The counter value.
   */
  private static int count = CounterConfiguration.DEFAULT_VALUE;
  /**
   * The lock available flag to avoid busy waiting.
   */
  private static volatile boolean lock = false;

  @Override
  public void add(final int value) {
    while (true) {
      if (!lock) {
        synchronized (PollingCounter.class) {
          lock = true;
          count += value;
          lock = false;
          break;
        }
      }
    }
  }

  @Override
  public int show() {
    return count;
  }

  @Override
  public void clear() {
    while (true) {
      if (!lock) {
        synchronized (PollingCounter.class) {
          lock = true;
          count = 0;
          lock = false;
          break;
        }
      }
    }
  }
}
