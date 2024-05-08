package com.concurrency.jpa.customer.order.service;

import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface OrderService {
    PaymentStatusDto createOrder(CreateOrderRequestDto createOrderRequestDto);
    Order getOrder(CreateOrderRequestDto createOrderRequestDto, List<ActualProduct> actualProducts);

    void rollback(Long paymentId);

    void changeActualProductStatus(Long paymentId);

    OrderDto findByPaymentId(long l);

}
