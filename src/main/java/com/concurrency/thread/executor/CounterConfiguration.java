package com.concurrency.thread.executor;

import com.concurrency.thread.counter.AtomicCounter;
import com.concurrency.thread.counter.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CounterConfiguration {

    @Bean
    public Counter counter() {
        return new AtomicCounter();
    }

    @Bean
    public CounterBenchmarkOptions counterConfig(Counter counter) {
        int iterations = 25;
        int totalRequest = Integer.MAX_VALUE / 1024;
        int nThreads = 1;
        return new CounterBenchmarkOptions(counter, iterations, totalRequest, nThreads);
    }

    public record CounterConfig(Counter counter, int iterations, int totalRequests, int nThreads) {
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
}
