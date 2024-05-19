package com.concurrency.jpa.customer.payment.service;

import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;

public interface PaymentService {

    PaymentStatusDto confirm(PaymentStatusDto dto) throws InterruptedException;

    OrderDto waitUntilFinish(PaymentStatusDto dto) throws InterruptedException;

    PaymentStatusDto cancel(PaymentStatusDto dto);
}
