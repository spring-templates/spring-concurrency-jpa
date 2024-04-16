package com.concurrency.thread.counter.impl.batch;

import com.concurrency.thread.counter.impl.Counter;

/**
 * BatchCounter interface is an extension of Counter interface. It adds a flush
 * method to flush the batch value to the counter value.
 */
public interface BatchCounter extends Counter {

  /**
   * Flush the batch value to the counter value.
   */
  void flush();
}
