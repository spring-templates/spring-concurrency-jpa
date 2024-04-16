package com.concurrency.thread.counter.executor;

import com.concurrency.thread.counter.impl.Counter;

public record CounterBenchmarkOptions(
    Counter counter,
    int iterations,
    int totalRequests,
    int threads
) {

  @Override
  public String toString() {
    return """
        CounterConfig {
            counter=%s,
            iterations=%d,
            totalRequests=%d,
            nThreads=%d
        }""".stripIndent()
        .formatted(counter.getClass().getSimpleName(),
                   iterations, totalRequests, threads);
  }
}
