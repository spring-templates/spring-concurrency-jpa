package com.thread.concurrency.counter.batch;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile("dev")
public class ConcurrentParameterizedBatchingCounter implements BatchCounter {
    private static final int BATCH_SIZE = 100;
    private final AtomicLong counter = new AtomicLong();
    private final ConcurrentMap<Long, List<Integer>> batch = new ConcurrentHashMap<>();

    @Override
    public void add(int value) {
        var threadId = Thread.currentThread().threadId();
        batch.computeIfAbsent(threadId, k -> new ArrayList<>()).add(value);
        if (batch.get(threadId).size() >= BATCH_SIZE) {
            flush(threadId);
        }
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

    private void flush(long threadId) {
        var list = batch.getOrDefault(threadId, null);
        if (list != null && !list.isEmpty()) {
            counter.addAndGet(list.stream().mapToLong(Integer::longValue).sum());
            batch.remove(threadId);
        }
    }

    @Override
    public void flush() {
        var threadId = Thread.currentThread().threadId();
        flush(threadId);
    }
}
