package com.thread.concurrency.counter.batch;

import com.thread.concurrency.counter.Counter;

public interface BatchCounter extends Counter {
    void flush();
}
