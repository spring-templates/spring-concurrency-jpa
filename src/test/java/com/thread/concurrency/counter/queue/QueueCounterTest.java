package com.thread.concurrency.counter.queue;

import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.*;

public class QueueCounterTest {
    private static final int valueToAdd = 1;
    private static final int nAddsPerThread = 100000;
    private static final int producerNThreads = 9;
    private static final int consumerNThreads = 9;
    private Consumer consumer;
    private Producer producer;
    private ExecutorService consumerExecutor;
    private ExecutorService producerExecutor;

    @BeforeEach
    public void setup() {
        int queueCapacity = 1000;
        BlockingQueue<Long> queue = new LinkedBlockingQueue<>(queueCapacity);
        consumer = new CounterConsumer(queue);
        producer = new CounterProducer(queue);
        producerExecutor = Executors.newFixedThreadPool(producerNThreads);
        consumerExecutor = Executors.newFixedThreadPool(consumerNThreads);
    }

    @AfterEach
    public void cleanup() {
        producerExecutor.shutdown();
        consumerExecutor.shutdown();
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    @DisplayName("멀티 프로듀서 싱글 컨슈머")
    public void multiProducerSingleConsumer() {
        Assertions.assertTimeout(Duration.ofSeconds(10), () -> runTest(1));
    }

    private void runTest(int consumerCount) throws InterruptedException {
        Long initialCount = consumer.show();
        CountDownLatch producerLatch = new CountDownLatch(producerNThreads);
        CountDownLatch consumerLatch = new CountDownLatch(consumerCount);

        createProducerThreads(producerLatch);
        createConsumerThreads(consumerLatch, consumerCount);

        consumerLatch.await();
        producerLatch.await();

        Long finalCount = consumer.show();
        Assertions.assertEquals(initialCount + nAddsPerThread * producerNThreads * valueToAdd, finalCount);
    }

    private void createProducerThreads(CountDownLatch producerLatch) {
        Callable<Void> task = () -> {
            for (int j = 0; j < nAddsPerThread; j++) {
                producer.add(valueToAdd);
            }
            producerLatch.countDown();
            return null;
        };
        for (int i = 0; i < producerNThreads; i++) {
            producerExecutor.submit(task);
        }
    }

    private void createConsumerThreads(CountDownLatch consumerLatch, int consumerCount) {
        Callable<Void> task = () -> {
            consumer.consumeEvent(1, TimeUnit.SECONDS);
            consumerLatch.countDown();
            return null;
        };
        for (int i = 0; i < consumerCount; i++) {
            consumerExecutor.submit(task);
        }
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    @DisplayName("멀티 프로듀서 멀티 컨슈머")
    public void multiProducerMultiConsumer() {
        Assertions.assertTimeout(Duration.ofSeconds(10), () -> runTest(consumerNThreads));
    }
}
