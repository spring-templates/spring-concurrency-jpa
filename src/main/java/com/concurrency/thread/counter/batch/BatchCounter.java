package com.concurrency.thread.counter.batch;

import com.concurrency.thread.counter.Counter;

public interface BatchCounter extends Counter {
    void flush();
}
