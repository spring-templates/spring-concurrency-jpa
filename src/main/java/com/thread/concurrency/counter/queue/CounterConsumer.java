package com.thread.concurrency.counter.queue;

import org.springframework.context.annotation.Profile;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Profile("dev")
public class CounterConsumer implements Consumer {
    private final BlockingQueue<Long> queue;
    private final AtomicLong count = new AtomicLong(0);

    public CounterConsumer(BlockingQueue<Long> queue) {
        this.queue = queue;
    }

    public void consumeEvent(long timeout, TimeUnit unit) throws InterruptedException {
        while (true) {
            Long value = queue.poll(timeout, unit);
            if (value == null) {
                break;
            }
            count.addAndGet(value);
        }
    }

    public Long show() {
        while (true) {
            if (queue.isEmpty()) {
                return count.get();
            }
        }
    }
}
