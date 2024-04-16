package com.concurrency.thread.counter.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public final class CounterTest {

  /**
   * The value to add.
   */
  public static final int ADD_PER_THREAD = 1000;
  /**
   * The value to add.
   */
  public static final int THREADS = 100;

  private static Stream<Counter> counterProvider() {
    return Stream.of(new LockCounter(), new PollingCounter(),
                     new SynchronizedCounter(), new AtomicCounter(),
                     new CompletableFutureCounter());
  }

  /**
   * A stress test for the counter.
   *
   * @param counter the counter to test.
   */
  @ParameterizedTest
  @MethodSource("counterProvider")
  public void stressTest(final Counter counter) {
    // given
    int threads = THREADS;
    int addPerThread = ADD_PER_THREAD;
    int expectedValue = counter.show() + threads * addPerThread;

    // when
    long start = System.currentTimeMillis();
    whenAdd(counter, threads, addPerThread);
    long end = System.currentTimeMillis();

    // then
    Assertions.assertEquals(expectedValue, counter.show());
    System.out.println("Time elapsed: " + (end - start) + "ms");
  }

  private void whenAdd(
      final Counter counter, final int threads, final int addPerThread
  ) {
    try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
      for (int i = 0; i < threads; i++) {
        executor.submit(() -> {
          for (int j = 0; j < addPerThread; j++) {
            counter.add(1);
          }
        });
      }
    }
  }

  /**
   * A test for the clear method.
   *
   * @param counter the counter to test.
   */
  @ParameterizedTest
  @MethodSource("counterProvider")
  public void clearTest(final Counter counter) {
    counter.add(ADD_PER_THREAD * THREADS);
    counter.clear();
    Assertions.assertEquals(0, counter.show());
  }
}
