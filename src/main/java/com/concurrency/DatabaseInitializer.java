package com.concurrency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public final class DatabaseInitializer implements CommandLineRunner {

  /**
   * The JDBC template.
   */
  private final JdbcTemplate jdbcTemplate;

  /**
   * The constructor.
   *
   * @param jdbcTemplate the JDBC template
   */
  @Autowired
  public DatabaseInitializer(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(final String... args) {
    jdbcTemplate.execute("DROP PROCEDURE IF EXISTS InsertSampleData");
    jdbcTemplate.execute("""
        CREATE PROCEDURE InsertSampleData()
        BEGIN
            -- Declare variables
            DECLARE i INT DEFAULT 1;
            DECLARE baseEmail VARCHAR(255) DEFAULT 'user';
            DECLARE baseProvider VARCHAR(255) DEFAULT 'provider';
            DECLARE baseFirstName VARCHAR(255) DEFAULT 'First';
            DECLARE baseLastName VARCHAR(255) DEFAULT 'Last';
            -- Loop 100 times
            WHILE i <= 100 DO
                -- Insert a new row into the customer table
                INSERT INTO customer(
                    email_id,
                    email_provider,
                    first_name,
                    last_name
                )
                VALUES (
                    CONCAT(baseEmail, i),             -- email_id
                    CONCAT(baseProvider, i, '.com'),  -- email_provider
                    CONCAT(baseFirstName, i),         -- first_name
                    CONCAT(baseLastName, i)           -- last_name
                );
                -- Increment the counter
                SET i = i + 1;
            END WHILE;
        END;
        """);
    jdbcTemplate.update("CALL InsertSampleData();");
  }
}
