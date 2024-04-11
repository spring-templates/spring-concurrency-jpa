package com.thread.concurrency.counter;

import org.springframework.stereotype.Component;

@Component
public class PollingCounter implements Counter {
    private static int count = 100;
    private static volatile boolean lock = false;

    @Override
    public void add(int value) {
        while (true) {
            if (!lock) {
                synchronized (PollingCounter.class) {
                    lock = true;
                    count += value;
                    lock = false;
                    break;
                }
            }
        }
    }

    @Override
    public int show() {
        return count;
    }

    @Override
    public void clear() {
        while (true) {
            if (!lock) {
                synchronized (PollingCounter.class) {
                    lock = true;
                    count = 0;
                    lock = false;
                    break;
                }
            }
        }
    }
}
