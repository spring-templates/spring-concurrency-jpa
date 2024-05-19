package com.concurrency.jpa.customer.lock;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface LockService {
  <T> T executeWithLock(String email,
                        int timeoutSeconds,
                        Supplier<T> supplier);

  <T> void executeWithLock(String email, int timeoutSeconds, T dto, Consumer<T> consumer);
}
