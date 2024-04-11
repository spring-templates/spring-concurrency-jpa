package com.thread.concurrency.counter;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockCounter implements Counter {
    private static final ReentrantLock lock = new ReentrantLock();
    private static int count = 100;

    @Override
    public void add(int value) {
        lock.lock();
        try {
            count += value;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int show() {
        return count;
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            count = 0;
        } finally {
            lock.unlock();
        }
    }
}
