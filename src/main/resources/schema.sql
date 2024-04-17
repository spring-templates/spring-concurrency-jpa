DROP TABLE IF EXISTS customer;
CREATE TABLE customer
(
    email_id       VARCHAR(255) NOT NULL,
    email_provider VARCHAR(255) NOT NULL,
    first_name     VARCHAR(255) NULL,
    last_name      VARCHAR(255) NULL,
    CONSTRAINT pk_customer PRIMARY KEY (email_id, email_provider)
);

# CREATE TABLE IF NOT EXISTS  core_product (
#                                              core_product_id BIGINT NOT NULL AUTO_INCREMENT,
#                                              price BIGINT,
#                                              stock BIGINT,
#                                              seller_id BIGINT,
#                                              PRIMARY KEY (core_product_id)
# );
# CREATE TABLE IF NOT EXISTS `order_table` (
#                                              order_id BIGINT NOT NULL AUTO_INCREMENT,
#                                              actor VARCHAR(255),
#                                              total_price BIGINT,
#                                              total_count BIGINT,
#                                              PRIMARY KEY (order_id)
# );
# CREATE TABLE IF NOT EXISTS actual_product (
#     actual_product_id BIGINT NOT NULL AUTO_INCREMENT,
#     actual_status VARCHAR(255),
#     core_product_id BIGINT NOT NULL,
#     order_id BIGINT,
#     actual_price BIGINT,
#     discount_rate FLOAT,
#     PRIMARY KEY (actual_product_id),
#     FOREIGN KEY (core_product_id) REFERENCES core_product(core_product_id),
#     FOREIGN KEY (order_id) REFERENCES order_table(order_id)
# );


