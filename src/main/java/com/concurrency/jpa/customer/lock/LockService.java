package com.concurrency.jpa.customer.lock;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface LockService {
  <T> T executeWithLock(Long paymentId,
                        int timeoutSeconds,
                        Supplier<T> supplier);

  <T> void executeWithLock(Long paymentId, int timeoutSeconds, T dto, Consumer<T> consumer);
}
