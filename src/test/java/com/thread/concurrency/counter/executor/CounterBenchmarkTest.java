package com.thread.concurrency.counter.executor;

import com.thread.concurrency.executor.CounterBenchmark;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CounterBenchmarkTest {

    private final CounterBenchmark counterBenchmark;

    @Autowired
    public CounterBenchmarkTest(CounterBenchmark counterBenchmark) {
        this.counterBenchmark = counterBenchmark;
    }

    @Test
    void test() {
        var performance = counterBenchmark.benchmark();
        Assertions.assertNotNull(performance);
    }
}
