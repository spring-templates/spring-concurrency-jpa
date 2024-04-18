package com.concurrency.jpa.customer.Product;

import java.util.Map;

public interface ProductService {
    void validateCoreProducts(Map<Long, Long> requireProducts);
}
