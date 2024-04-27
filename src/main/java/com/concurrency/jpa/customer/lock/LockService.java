package com.concurrency.jpa.customer.lock;

public interface LockService {
  String lock(Long orderId);
  void unlock(String key);
}
