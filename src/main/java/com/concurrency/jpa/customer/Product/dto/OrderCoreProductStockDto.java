package com.concurrency.jpa.customer.Product.dto;

import lombok.Value;

@Value
public class OrderCoreProductStockDto {
    Long coreProductId;
    Long reduceStock;
}
