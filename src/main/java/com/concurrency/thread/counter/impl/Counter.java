package com.concurrency.thread.counter.impl;

public interface Counter {

  /**
   * Add a value to the counter.
   *
   * @param value the value to add
   */
  void add(int value);

  /**
   * Show the counter value.
   *
   * @return the counter value
   */
  int show();

  /**
   * Clear the counter.
   */
  void clear();
}
