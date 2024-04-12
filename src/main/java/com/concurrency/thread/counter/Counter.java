package com.concurrency.thread.counter;

public interface Counter {
    void add(int value);

    int show();

    void clear();
}
