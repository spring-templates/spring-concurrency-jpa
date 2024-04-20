package com.concurrency.jpa.customer.order.service;

import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface OrderService {
    @Transactional
    Order getOrder(CreateOrderRequestDto createOrderRequestDto, List<ActualProduct> actualProducts);

    @Transactional(readOnly = true)
    List<ActualProduct> concatActualProductList(Map<Long, Long> coreProducts);

    @Transactional(readOnly = true)
    List<ActualProduct> findActualProducts(Long coreProductId, ActualStatus actualStatus, Long stock);

    void updateCoreProductsStock(Map<Long, Long> requireProducts);
    OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto);

    @Transactional
    long subtractCoreProductStock(Long coreProductId, Long reqStock);
}
