package com.concurrency.jpa.customer.lock;

import java.util.function.Supplier;

public interface LockService {
  <T> T executeWithLock(String userLockName,
                        int timeoutSeconds,
                        Supplier<T> supplier);
}
