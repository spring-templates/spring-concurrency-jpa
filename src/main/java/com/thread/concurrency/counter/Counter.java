package com.thread.concurrency.counter;

public interface Counter {
    void add(int value);

    int show();

    void clear();
}
