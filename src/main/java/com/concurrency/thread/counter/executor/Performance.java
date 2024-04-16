package com.concurrency.thread.counter.executor;

public record Performance(
    String name,
    long time,
    int threads,
    long memory
) {

  @Override
  public String toString() {
    return String.format(
        "| %-20s | %10d ms | %5d threads | %10d KB |",
        name, time, threads, memory / CounterConfiguration.REQUEST_DIVIDER);
  }
}
