package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    OrderService orderService;


}
