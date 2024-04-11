package com.thread.concurrency.counter;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AtomicCounter implements Counter {
    private final AtomicInteger count = new AtomicInteger(100);

    @Override
    public void add(int value) {
        count.addAndGet(value);
    }

    @Override
    public int show() {
        return count.get();
    }

    @Override
    public void clear() {
        count.set(0);
    }
}
