package com.concurrency.jpa.customer.lock;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;

@Service
@RequiredArgsConstructor
public class LockServiceImpl implements LockService{
    private static final String MY_LOCK_KEY = "someLockKey_";
    private final LockRegistry lockRegistry;
    @Override
    public String lock(Long orderId) {
        String key = MY_LOCK_KEY+orderId;
        Lock lock = null;
        try {
            lock = lockRegistry.obtain(key);
        } catch (Exception e) {
            // in a production environment this should be a log statement
            System.out.println(String.format("Unable to obtain lock: %s", MY_LOCK_KEY));
        }
//        String returnVal = null;
        try {
            if (lock.tryLock()) {
//                returnVal = "jdbc lock successful";
                // 여기서 외부 작업을 수행하면 좋겠다.
                System.out.println("jdbc lock successful");
            } else {
//                returnVal = "jdbc lock unsuccessful";
                System.out.println("jdbc lock unsuccessful");
            }
        } catch (Exception e) {
            // in a production environment this should log and do something else
            e.printStackTrace();
            lock.unlock();
        }
        System.out.println("key : "+key);
        return key;
    }

    @Override
    public void unlock(String key) {
        Lock lock = null;
        try {
            lock = lockRegistry.obtain(key);
        } catch (Exception e) {
            // in a production environment this should be a log statement
            System.out.println(String.format("Unable to obtain lock: %s", key));
        } finally {
            lock.unlock();
        }
    }

}
