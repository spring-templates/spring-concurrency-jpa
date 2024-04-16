package com.concurrency.thread.counter.executor;

import com.concurrency.thread.counter.impl.Counter;
import com.concurrency.thread.counter.impl.batch.BatchCounter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * CounterBenchmark class to benchmark the counter performance.
 */
@Component
public class CounterBenchmark {

  /**
   * The counter to benchmark.
   */
  private final Counter counter;
  /**
   * The name of the counter.
   */
  private final String counterName;
  /**
   * The number of iterations to run.
   */
  private final int iterations;
  /**
   * The number of threads to use.
   */
  private final int threads;
  /**
   * The total number of requests to make.
   */
  private final int totalRequests;

  /**
   * Constructs a new CounterBenchmark with the given configuration.
   *
   * @param config The configuration options for the benchmark.
   */
  @Autowired
  public CounterBenchmark(final CounterBenchmarkOptions config) {
    System.out.println(config);
    this.counter = config.counter();
    this.counterName = counter.getClass().getSimpleName();
    this.iterations = config.iterations();
    this.threads = config.threads();
    this.totalRequests = config.totalRequests();
  }

  /**
   * Benchmark the counter.
   *
   * @return the performance
   */
  public Performance run() {
    TableColumns.printHeader();
    TableColumns.printSeparator();
    List<Performance> performances = new ArrayList<>();
    for (int i = 0; i < iterations; i++) {
      var performance = calculateEach();
      performances.add(performance);
      System.out.println(performance);
    }
    return reduce(performances);
  }

  private Performance calculateEach() {
    List<Integer> iterPerThread = range();
    Consumer<Integer> task = (Integer requests) -> {
      for (int i = 0; i < requests; i++) {
        counter.add(1);
      }
      if (counter instanceof BatchCounter batchCounter) {
        batchCounter.flush();
      }
    };
    System.gc();
    long timeOnStart = System.currentTimeMillis();
    long memoryOnStart =
        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    doAdd(iterPerThread, task);
    if (counter.show() != totalRequests) {
      System.out.printf("Counter: %d, Total: %d%n", counter.show(),
          totalRequests
      );
    }

    long timeOnEnd = System.currentTimeMillis();
    long memoryOnEnd =
        Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    long timeElapsed = timeOnEnd - timeOnStart;
    long memoryUsed = memoryOnEnd - memoryOnStart;
    return new Performance(counterName, timeElapsed, iterPerThread.size(),
        memoryUsed
    );
  }

  private void doAdd(final List<Integer> params, final Consumer<Integer> task) {
    try (
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()
    ) {
      counter.clear();
      List<CompletableFuture<Void>> futures = new ArrayList<>();
      for (int i = 0; i < threads; i++) {
        int requests = params.get(i);
        futures.add(
            CompletableFuture.runAsync(() -> task.accept(requests), executor));
      }
      futures.forEach(CompletableFuture::join);
    }
  }

  private List<Integer> range() {
    int baseValue = totalRequests / threads;
    int remainder = totalRequests % threads;

    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      var remainderValue = i < remainder ? 1 : 0;
      result.add(baseValue + remainderValue);
    }
    assert result.stream().mapToInt(Integer::intValue).sum() == totalRequests;
    return result;
  }

  private Performance reduce(final List<Performance> performances) {
    performances.sort((a, b) -> (int) (a.time() - b.time()));
    long time = performances.get(performances.size() / 2).time();
    performances.sort((a, b) -> (int) (a.memory() - b.memory()));
    long memory = performances.get(performances.size() / 2).memory();
    return new Performance(counterName, time, threads, memory);
  }
}
