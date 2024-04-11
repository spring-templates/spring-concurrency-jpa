package com.thread.concurrency.counter.queue;

import org.springframework.context.annotation.Profile;

import java.util.concurrent.BlockingQueue;

@Profile("dev")
public class CounterProducer implements Producer {
    private final BlockingQueue<Long> queue;

    public CounterProducer(BlockingQueue<Long> queue) {
        this.queue = queue;
    }

    public void add(long value) throws InterruptedException {
        queue.put(value);
    }
}
