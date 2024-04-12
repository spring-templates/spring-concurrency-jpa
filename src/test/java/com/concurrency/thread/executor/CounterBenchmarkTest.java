package com.concurrency.thread.executor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CounterConfiguration.class)
public class CounterBenchmarkTest {

    @Autowired
    private CounterBenchmark counterBenchmark;

    @Test
    void test() {
        var performance = counterBenchmark.benchmark();
        Assertions.assertNotNull(performance);
    }
}
