package com.thread.concurrency.counter.batch;

import com.thread.concurrency.counter.Counter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class BatchCounterTest {

    public static Stream<Counter> batchCounterProvider() {
        return Stream.of(new AtomicBatchCounter(), new ConcurrentParameterizedBatchingCounter());
    }

    @ParameterizedTest
    @MethodSource("batchCounterProvider")
    void clearTest(BatchCounter counter) {
        counter.add(1000);
        counter.clear();
        Assertions.assertEquals(0, counter.show());
    }

    @ParameterizedTest
    @MethodSource("batchCounterProvider")
    void singleThreading(BatchCounter counter) {
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

    private static List<Integer> range() {
        return IntStream.range(0, 1000).boxed().collect(Collectors.toList());
    }

    @ParameterizedTest
    @MethodSource("batchCounterProvider")
    void conditionalMultiThreading(BatchCounter counter) {
        // given
        int numberOfThreads = 2;
        int expected = Integer.MAX_VALUE / 1024;
        List<Integer> iterPerThread = range(numberOfThreads, expected);
        Consumer<Integer> task = (Integer number) -> {
            for (int i = 0; i < number; i++) {
                counter.add(1);
            }
            counter.flush();
        };
        // when
        try (ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads)) {
            for (int num : iterPerThread) {
                executor.submit(() -> task.accept(num));
            }
        }
        // then
        Assertions.assertEquals(expected, counter.show());
    }

    private static List<Integer> range(int numberOfThreads, int expected) {
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
    void conditionalAsyncVirtualMultiThreading(BatchCounter counter) {
        // given
        int numberOfThreads = 2;
        int expected = Integer.MAX_VALUE / 1024;
        List<Integer> iterPerThread = range(numberOfThreads, expected);
        Consumer<Integer> task = (Integer number) -> {
            for (int i = 0; i < number; i++) {
                counter.add(1);
            }
            counter.flush();
        };
        // when
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = iterPerThread.stream().map(number -> CompletableFuture.runAsync(() -> task.accept(number), executor)).toList();
            futures.forEach(CompletableFuture::join);
        }
        // then
        Assertions.assertEquals(expected, counter.show());
    }
}
