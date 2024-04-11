package com.thread.concurrency;

import com.thread.concurrency.executor.CounterBenchmark;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringThreadConcurrencyApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringThreadConcurrencyApplication.class, args);
        var performance = context.getBean(CounterBenchmark.class).benchmark();
        System.out.println("|----------------------|---------------|---------------|---------------|");
        System.out.println(performance);
    }
}
