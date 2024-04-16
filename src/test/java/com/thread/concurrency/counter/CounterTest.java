package com.thread.concurrency.counter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class CounterTest {

    public static Stream<Counter> counterProvider() {
        return Stream.of(new LockCounter(), new PollingCounter(), new SynchronizedCounter(), new AtomicCounter(), new CompletableFutureCounter());
    }

    @ParameterizedTest
    @MethodSource("counterProvider")
    public void stressTest(Counter counter) {
        // given
        int nThreads = 100;
        int addPerThread = 1000;
        int expectedValue = counter.show() + nThreads * addPerThread;

        // when
        long start = System.currentTimeMillis();
        whenAdd(counter, nThreads, addPerThread);
        long end = System.currentTimeMillis();

        // then
        Assertions.assertEquals(expectedValue, counter.show());
        System.out.println("Time elapsed: " + (end - start) + "ms");
    }

    private void whenAdd(Counter counter, int nThreads, int addPerThread) {
        try (ExecutorService executor = Executors.newFixedThreadPool(nThreads)) {
            for (int i = 0; i < nThreads; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < addPerThread; j++) {
                        counter.add(1);
                    }
                });
            }
        }
    }

    @ParameterizedTest
    @MethodSource("counterProvider")
    public void clearTest(Counter counter) {
        counter.add(1000);
        counter.clear();
        Assertions.assertEquals(0, counter.show());
    }
}
