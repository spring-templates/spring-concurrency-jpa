package com.thread.concurrency.executor;

import com.thread.concurrency.counter.Counter;
import com.thread.concurrency.counter.batch.BatchCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


@Component
public class CounterBenchmark {
    private final Counter counter;
    private final String counterName;
    private final int iterations, nThreads, totalRequests;

    @Autowired
    public CounterBenchmark(CounterConfiguration.CounterConfig config) {
        System.out.println(config);
        this.counter = config.counter();
        this.counterName = counter.getClass().getSimpleName();
        this.iterations = config.iterations();
        this.nThreads = config.nThreads();
        this.totalRequests = config.totalRequests();
    }

    public Performance benchmark() {
        System.out.printf("| %-20s | %13s | %13s | %13s |%n", "Name", "Time", "Threads", "Memory");
        System.out.println("|----------------------|---------------|---------------|---------------|");
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
        Consumer<Integer> task = (Integer nRequest) -> {
            for (int i = 0; i < nRequest; i++) {
                counter.add(1);
            }
            if (counter instanceof BatchCounter batchCounter) {
                batchCounter.flush();
            }
        };
        System.gc();
        long timeOnStart = System.currentTimeMillis();
        long memoryOnStart = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        doAdd(iterPerThread, task);
        if (counter.show() != totalRequests) {
            System.out.printf("Counter: %d, Total: %d%n", counter.show(), totalRequests);
        }


        long timeOnEnd = System.currentTimeMillis();
        long memoryOnEnd = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();


        long timeElapsed = timeOnEnd - timeOnStart;
        long memoryUsed = memoryOnEnd - memoryOnStart;
        return new Performance(counterName, timeElapsed, iterPerThread.size(), memoryUsed);
    }

    private void doAdd(List<Integer> params, Consumer<Integer> task) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            counter.clear();
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < nThreads; i++) {
                int nRequest = params.get(i);
                futures.add(CompletableFuture.runAsync(() -> task.accept(nRequest), executor));
            }
            futures.forEach(CompletableFuture::join);
        }
    }

    private List<Integer> range() {
        int baseValue = totalRequests / nThreads;
        int remainder = totalRequests % nThreads;

        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < nThreads; i++) {
            var remainderValue = i < remainder ? 1 : 0;
            result.add(baseValue + remainderValue);
        }
        assert result.stream().mapToInt(Integer::intValue).sum() == totalRequests;
        return result;
    }

    private Performance reduce(List<Performance> performances) {
        performances.sort((a, b) -> (int) (a.time() - b.time()));
        long time = performances.get(performances.size() / 2).time();
        performances.sort((a, b) -> (int) (a.memory() - b.memory()));
        long memory = performances.get(performances.size() / 2).memory();
        return new Performance(counterName, time, nThreads, memory);
    }
}
