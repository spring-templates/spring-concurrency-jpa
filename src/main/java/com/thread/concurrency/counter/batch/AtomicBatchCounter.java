package com.thread.concurrency.counter.batch;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Component
public class AtomicBatchCounter implements BatchCounter {
    private final AtomicLong counter = new AtomicLong();
    private final ConcurrentMap<Long, LongAdder> batch = new ConcurrentHashMap<>();

    @Override
    public void add(int value) {
        var threadId = Thread.currentThread().threadId();
        batch.computeIfAbsent(threadId, k -> new LongAdder()).add(value);
    }

    @Override
    public int show() {
        return counter.intValue();
    }

    @Override
    public void clear() {
        counter.set(0);
        batch.clear();
    }

    @Override
    public void flush() {
        var threadId = Thread.currentThread().threadId();
        flush(threadId);
    }

    private void flush(long threadId) {
        var value = batch.remove(threadId);
        if (value != null) {
            counter.addAndGet(value.longValue());
        }
    }
}
