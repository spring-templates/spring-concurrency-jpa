package com.concurrency;

import com.concurrency.thread.executor.CounterConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CounterConfiguration.class)
class SpringConcurrencyApplicationTests {
    @Test
    @SuppressWarnings("EmptyMethod")
    void contextLoads() {
    }
}
