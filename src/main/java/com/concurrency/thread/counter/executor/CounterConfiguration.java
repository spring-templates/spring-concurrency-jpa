package com.concurrency.thread.counter.executor;

import com.concurrency.thread.counter.impl.Counter;
import com.concurrency.thread.counter.impl.batch.AtomicBatchCounter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CounterConfiguration {

  /**
   * The default value of the counter.
   */
  public static final int DEFAULT_VALUE = 100;
  /**
   * The number of iterations.
   */
  public static final int ITERATIONS = 25;
  /**
   * The request divider.
   */
  public static final int REQUEST_DIVIDER = 1024;

  /**
   * The default counter.
   *
   * @return the default counter
   */
  @Bean
  public Counter counter() {
    return new AtomicBatchCounter();
  }

  /**
   * The configuration for the counter benchmark.
   *
   * @param counter the counter
   * @return the configuration
   */
  @Bean
  public CounterBenchmarkOptions counterConfig(final Counter counter) {
    int totalRequest = Integer.MAX_VALUE / REQUEST_DIVIDER;
    int threads = 1;
    return new CounterBenchmarkOptions(
        counter, ITERATIONS, totalRequest, threads);
  }

  /**
   * The counter benchmark.
   *
   * @param config the configuration
   * @return the counter benchmark
   */
  @Bean
  public CounterBenchmark counterBenchmark(
      final CounterBenchmarkOptions config
  ) {
    return new CounterBenchmark(config);
  }
}
