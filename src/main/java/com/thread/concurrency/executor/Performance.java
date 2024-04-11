package com.thread.concurrency.executor;

public record Performance(String name, long time, int threads, long memory) {
    @Override
    public String toString() {
        return String.format("| %-20s | %10d ms | %5d threads | %10d KB |", name, time, threads, memory / 1024);
    }
}
