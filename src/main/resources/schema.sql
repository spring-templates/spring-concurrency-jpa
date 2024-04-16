DROP TABLE IF EXISTS customer;
CREATE TABLE customer
(
    email_id       VARCHAR(255) NOT NULL,
    email_provider VARCHAR(255) NOT NULL,
    first_name     VARCHAR(255) NULL,
    last_name      VARCHAR(255) NULL,
    CONSTRAINT pk_customer PRIMARY KEY (email_id, email_provider)
);

