package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.order.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class OrderServiceTest {
    @Autowired
    OrderService orderService;

    @Test
    public void shouldProductStockException(){
        Map<Long, Long> requireStock = new HashMap<>();
        requireStock.put((long) 1, (long) 10);
        requireStock.put((long) 2, (long) 5);
        Assertions.assertThrows(BaseException.class, () -> orderService.updateCoreProducts(requireStock));
    }
    @Test
    public void shouldProductStockPass(){
        Map<Long, Long> requireStock = new HashMap<>();
        requireStock.put((long) 1, (long) 1);
        requireStock.put((long) 2, (long) 2);
        Assertions.assertDoesNotThrow(() -> orderService.updateCoreProducts(requireStock));
    }
}
