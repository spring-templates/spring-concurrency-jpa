package com.concurrency.thread.counter.executor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CounterConfiguration.class)
public class CounterBenchmarkTest {

  /**
   * The counter benchmark.
   */
  @Autowired
  private CounterBenchmark benchmark;

  @Test
  void test() {
    var performance = benchmark.run();
    Assertions.assertNotNull(performance);
  }
}
