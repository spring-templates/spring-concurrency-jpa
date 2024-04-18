package com.concurrency.jpa.product;

import com.concurrency.jpa.customer.Product.ProductService;
import com.concurrency.jpa.customer.common.BaseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ProductServiceTest {
    @Autowired
    ProductService productService;

    @Test
    public void shouldProductStockException(){
        Map<Long, Long> requireStock = new HashMap<>();
        requireStock.put((long) 1, (long) 10);
        requireStock.put((long) 2, (long) 5);
        Assertions.assertThrows(BaseException.class, () -> productService.validateCoreProducts(requireStock));
    }
    @Test
    public void shouldProductStockPass(){
        Map<Long, Long> requireStock = new HashMap<>();
        requireStock.put((long) 1, (long) 1);
        requireStock.put((long) 2, (long) 2);
        Assertions.assertDoesNotThrow(() -> productService.validateCoreProducts(requireStock));
    }
}
