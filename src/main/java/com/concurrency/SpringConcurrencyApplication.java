package com.concurrency;

import com.concurrency.thread.executor.CounterBenchmark;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringConcurrencyApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringConcurrencyApplication.class, args);
        runThreadBenchmark(context);
    }

    private static void runThreadBenchmark(ConfigurableApplicationContext context) {
        var performance = context.getBean(CounterBenchmark.class).benchmark();
        System.out.println("|----------------------|---------------|---------------|---------------|");
        System.out.println(performance);
    }

}
