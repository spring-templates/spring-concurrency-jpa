package com.concurrency.jpa.customer.payment.service;

import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import org.springframework.stereotype.Service;

/**
 * 외부 결제 서버에 결제 요청, 결과 확인, 결제 취소를 요청하는 부분
 */
@Service
public class PaymentServiceImpl implements PaymentService{
    @Override
    public PaymentStatusDto pay(PaymentInitialRequestDto dto) {
        return null;
    }

    @Override
    public PaymentStatusDto confirm(PaymentStatusDto dto) {
        return null;
    }

    @Override
    public PaymentStatusDto cancel(PaymentStatusDto dto) {
        return null;
    }
}
