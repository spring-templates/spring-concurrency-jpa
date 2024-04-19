INSERT INTO customer(email_id, email_provider, first_name, last_name)
VALUES ('user', 'example.com', 'First', 'Last');

INSERT INTO core_product( price, seller_id, stock)
VALUES (1000, 1, 1000);

INSERT INTO core_product( price, seller_id, stock)
VALUES (5000, 1, 1000);

INSERT INTO actual_product(core_product_id, actual_status, actual_price, discount_rate, order_id)
VALUES (1, 'PENDING_ORDER', 1000, 0, null);

INSERT INTO actual_product(core_product_id, actual_status, actual_price, discount_rate, order_id)
VALUES (1, 'PENDING_ORDER', 900, 10, null);

INSERT INTO actual_product(core_product_id, actual_status, actual_price, discount_rate, order_id)
VALUES (2, 'PENDING_ORDER', 5000, 0, null);

INSERT INTO actual_product(core_product_id, actual_status, actual_price, discount_rate, order_id)
VALUES (2, 'PENDING_ORDER', 2500, 50, null);