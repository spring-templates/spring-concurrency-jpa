package com.concurrency.jpa.customer.payment.service;

import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;

public interface PaymentService {
    PaymentStatusDto pay(PaymentInitialRequestDto dto);

    PaymentStatusDto confirm(PaymentStatusDto dto);

    PaymentStatusDto cancel(PaymentStatusDto dto);
}
