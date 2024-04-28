package com.concurrency.jpa.customer.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

@Service
public class LockServiceImpl implements LockService{

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private static final String GET_LOCK = "SELECT GET_LOCK(?, ?)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";
    private static final String EXCEPTION_MESSAGE = "LOCK 을 수행하는 중에 오류가 발생하였습니다.";

    private final DataSource dataSource;

    private final LockRegistry lockRegistry;

    public LockServiceImpl(DataSource dataSource, LockRegistry lockRegistry) {
        this.dataSource = dataSource;
        this.lockRegistry = lockRegistry;
    }

    @Override
    public <T> T executeWithLock(String userLockName,
                                 int timeoutSeconds,
                                 Supplier<T> supplier) {
        var lock = lockRegistry.obtain(userLockName);
        boolean lockAcquired =  lock.tryLock();
        if(lockAcquired){
            try{
                log.info("lock taken");
                return supplier.get();
            }
            finally {
                lock.unlock();
            }
        }
        else{
            throw new RuntimeException(EXCEPTION_MESSAGE);
        }
    }

    private void getLock(Connection connection,
                         String userLockName,
                         int timeoutseconds) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(GET_LOCK)) {
            preparedStatement.setString(1, userLockName);
            preparedStatement.setInt(2, timeoutseconds);

            checkResultSet(userLockName, preparedStatement, "GetLock_");
        }
    }

    private void releaseLock(Connection connection,
                             String userLockName) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(RELEASE_LOCK)) {
            preparedStatement.setString(1, userLockName);

            checkResultSet(userLockName, preparedStatement, "ReleaseLock_");
        }
    }

    private void checkResultSet(String userLockName,
                                PreparedStatement preparedStatement,
                                String type) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
                log.error(String.format("USER LEVEL LOCK 쿼리 결과 값이 없습니다. type = %s, userLockName %s, connection=%s", type, userLockName, preparedStatement.getConnection()));
                throw new RuntimeException(EXCEPTION_MESSAGE);
            }
            int result = resultSet.getInt(1);
            if (result != 1) {
                log.error(String.format("USER LEVEL LOCK 쿼리 결과 값이 1이 아닙니다. type = %s, result %s userLockName %s, connection=%s", type, result, userLockName, preparedStatement.getConnection()));
                throw new RuntimeException(EXCEPTION_MESSAGE);
            }
        }
    }
}
