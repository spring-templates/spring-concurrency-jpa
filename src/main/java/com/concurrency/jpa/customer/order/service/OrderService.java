package com.concurrency.jpa.customer.order.service;

import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.dto.OrderDto;

import java.util.Map;

public interface OrderService {
    void updateCoreProducts(Map<Long, Long> requireProducts);
    OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto);
}
