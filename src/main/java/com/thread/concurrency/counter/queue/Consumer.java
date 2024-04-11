package com.thread.concurrency.counter.queue;

import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Profile("dev")
public interface Consumer {
    void consumeEvent(long timeout, TimeUnit unit) throws InterruptedException;

    Long show();
}
