package com.thread.concurrency.counter.queue;

import org.springframework.context.annotation.Profile;

@Profile("dev")
public interface Producer {
    void add(long value) throws InterruptedException;
}
