package com.concurrency.thread.counter.impl.batch;

import com.concurrency.thread.counter.impl.Counter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BatchCounterTest {

  /**
   * The end exclusive value.
   */
  public static final int END_EXCLUSIVE = 1000;
  /**
   * The divider for lower burden of the test.
   */
  public static final int DIVIDER = 1024;

  public static Stream<Counter> batchCounterProvider() {
    return Stream.of(
        new AtomicBatchCounter(),
        new ConcurrentParameterizedBatchingCounter());
  }

  private static List<Integer> range() {
    return IntStream.range(0, END_EXCLUSIVE).boxed()
        .collect(Collectors.toList());
  }

  private static List<Integer> range(
      final int numberOfThreads,
      final int expected
  ) {
    int baseValue = expected / numberOfThreads;
    int remainder = expected % numberOfThreads;

    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < numberOfThreads; i++) {
      if (i < remainder) {
        result.add(baseValue + 1);
      } else {
        result.add(baseValue);
      }
    }
    return result;
  }

  @ParameterizedTest
  @MethodSource("batchCounterProvider")
  void clearTest(final BatchCounter counter) {
    counter.add(END_EXCLUSIVE);
    counter.clear();
    Assertions.assertEquals(0, counter.show());
  }

  @ParameterizedTest
  @MethodSource("batchCounterProvider")
  void singleThreading(final BatchCounter counter) {
    // given
    var numbers = range();
    var partialSum = numbers.stream().reduce(0, Integer::sum);
    Runnable task = () -> {
      for (Integer number : numbers) {
        counter.add(number);
      }
      counter.flush();
    };
    // when
    task.run();
    // then
    Assertions.assertEquals(partialSum, counter.show());
  }

  @ParameterizedTest
  @MethodSource("batchCounterProvider")
  void conditionalMultiThreading(final BatchCounter counter) {
    // given
    int numberOfThreads = 2;
    int expected = Integer.MAX_VALUE / DIVIDER;
    List<Integer> iterPerThread = range(numberOfThreads, expected);
    Consumer<Integer> task = (Integer number) -> {
      for (int i = 0; i < number; i++) {
        counter.add(1);
      }
      counter.flush();
    };
    // when
    try (
        ExecutorService executor = Executors.newFixedThreadPool(
            numberOfThreads)
    ) {
      for (int num : iterPerThread) {
        executor.submit(() -> task.accept(num));
      }
    }
    // then
    Assertions.assertEquals(expected, counter.show());
  }

  @ParameterizedTest
  @MethodSource("batchCounterProvider")
  void conditionalAsyncVirtualMultiThreading(final BatchCounter counter) {
    // given
    int numberOfThreads = 2;
    int expected = Integer.MAX_VALUE / DIVIDER;
    List<Integer> iterPerThread = range(numberOfThreads, expected);
    Consumer<Integer> task = (Integer number) -> {
      for (int i = 0; i < number; i++) {
        counter.add(1);
      }
      counter.flush();
    };
    // when
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<CompletableFuture<Void>> futures = iterPerThread.stream().map(
          number -> CompletableFuture.runAsync(
              () -> task.accept(number),
              executor)).toList();
      futures.forEach(CompletableFuture::join);
    }
    // then
    Assertions.assertEquals(expected, counter.show());
  }
}
