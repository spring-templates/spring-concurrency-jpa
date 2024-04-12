package com.concurrency.thread.executor;

import com.concurrency.thread.counter.Counter;

public record CounterBenchmarkOptions(Counter counter, int iterations, int totalRequests, int nThreads) {
    @Override
    public String toString() {
        // multiple-lines format
        return """
            CounterConfig {
                counter=%s,
                iterations=%d,
                totalRequests=%d,
                nThreads=%d
            }""".formatted(counter.getClass().getSimpleName(), iterations, totalRequests, nThreads).stripIndent();
    }
}
