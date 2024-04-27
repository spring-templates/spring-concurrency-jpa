package com.concurrency.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;

import javax.sql.DataSource;

@Configuration
public class JDBCLockConfig {
    @Primary
    @Bean(name = "firstDataSourceProperties")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties firstDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "firstDataSource")
    @ConfigurationProperties("spring.datasource.configuration")
    public DataSource dataSource() {
        return firstDatasourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public DefaultLockRepository DefaultLockRepository(DataSource dataSource) {
        return new DefaultLockRepository(dataSource);
    }

    @Bean
    public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
        return new JdbcLockRegistry(lockRepository);
    }
}
