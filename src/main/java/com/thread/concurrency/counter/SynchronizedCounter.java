package com.thread.concurrency.counter;

import org.springframework.stereotype.Component;

@Component
public class SynchronizedCounter implements Counter {

    private int counter = 100;

    @Override
    public synchronized void add(int value) {
        counter += value;
    }

    @Override
    public synchronized int show() {
        return counter;
    }

    @Override
    public synchronized void clear() {
        counter = 0;
    }
}
