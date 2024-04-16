package com.thread.concurrency.counter;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class CompletableFutureCounter implements Counter {
    private CompletableFuture<Integer> counter;

    public CompletableFutureCounter() {
        this.counter = new CompletableFuture<>();
        counter.complete(100);
    }

    @Override
    public void add(int value) {
        synchronized (this) {
            counter = counter.thenApply((c) -> c + value);
        }
    }

    @Override
    public int show() {
        try {
            return counter.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            counter = CompletableFuture.completedFuture(0);
        }
    }
}
