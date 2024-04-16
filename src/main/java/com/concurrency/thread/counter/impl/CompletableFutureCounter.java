package com.concurrency.thread.counter.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Component;

@Component
public final class CompletableFutureCounter implements Counter {

  /**
   * The initial value.
   */
  public static final int INITIAL_VALUE = 100;
  /**
   * The counter value.
   */
  private CompletableFuture<Integer> counter;

  /**
   * The default constructor.
   */
  public CompletableFutureCounter() {
    this.counter = new CompletableFuture<>();
    counter.complete(INITIAL_VALUE);
  }

  @Override
  public void add(final int value) {
    synchronized (this) {
      counter = counter.thenApply((c) -> c + value);
    }
  }

  @Override
  public int show() {
    try {
      return counter.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void clear() {
    synchronized (this) {
      counter = CompletableFuture.completedFuture(0);
    }
  }
}
