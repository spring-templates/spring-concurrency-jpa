package com.concurrency.jpa.customer.payment.service;

import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;

public interface PaymentService {

    PaymentStatusDto confirm(PaymentStatusDto dto);

    OrderDto result(PaymentStatusDto dto);

    PaymentStatusDto cancel(PaymentStatusDto dto);
}
