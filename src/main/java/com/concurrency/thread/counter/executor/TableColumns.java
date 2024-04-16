package com.concurrency.thread.counter.executor;

import java.util.Arrays;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TableColumns {
  /**
   * The length of the column name.
   */
  NAME(22),
  /**
   * The length of the column time.
   */
  TIME(15),
  /**
   * The length of the column threads.
   */
  THREADS(15),
  /**
   * The length of the column memory.
   */
  MEMORY(15);

  /**
   * The length of the column.
   */
  private final int length;

  /**
   * Print the header.
   */
  public static void printHeader() {
    StringBuilder formatBuilder = new StringBuilder();
    for (TableColumns column : TableColumns.values()) {
      formatBuilder.append("| %-").append(column.getLength() - 2).append("s ");
    }
    formatBuilder.append("|%n");
    String format = formatBuilder.toString();
    System.out.printf(format, Arrays.stream(TableColumns.values()).toArray());
  }

  /**
   * Print the separator.
   */
  public static void printSeparator() {
    int[] columns =
        Stream.of(TableColumns.values()).mapToInt(TableColumns::getLength)
            .toArray();
    StringBuilder sb = new StringBuilder();
    for (int column : columns) {
      sb.append("|");
      sb.append("-".repeat(column));
    }
    sb.append("|");
    System.out.println(sb);
  }

}
