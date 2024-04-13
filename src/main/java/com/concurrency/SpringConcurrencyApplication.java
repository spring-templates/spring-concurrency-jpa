package com.concurrency;

import com.concurrency.thread.counter.executor.CounterBenchmark;
import com.concurrency.thread.counter.executor.TableColumns;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringConcurrencyApplication {

  /**
   * The main method.
   *
   * @param args the input arguments
   */
  public static void main(final String[] args) {
    ConfigurableApplicationContext context =
        SpringApplication.run(SpringConcurrencyApplication.class, args);
    runThreadBenchmark(context);
  }

  private static void runThreadBenchmark(
      final ConfigurableApplicationContext context
  ) {
    var performance = context.getBean(CounterBenchmark.class).run();
    TableColumns.printSeparator();
    System.out.println(performance);
  }
}
