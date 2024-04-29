package com.concurrency.jpa.customer.payment.dto;

import com.concurrency.jpa.customer.order.dto.OrderDto;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

public record PaymentInitialRequestDto(
    CustomerRequestDto seller,
    CustomerRequestDto buyer,
    AbstractPayment payment,
    Long price,
    // The URL to return to after the payment is completed.
    URI redirect
) {


    public PaymentInitialRequestDto(AbstractPayment abstractPayment, Long price){
        this(new CustomerRequestDto("abcSeller@gmail.com", "sol sol"),
            new CustomerRequestDto("abcBuyer@naver.com", "oh sol"),
                abstractPayment,
                price,
            URI.create("http://localhost:8080/payments/confirm"));
    }
}