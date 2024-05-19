package com.concurrency.jpa.customer.payment.dto;

public record PaymentStatusDto(
    Long paymentId,
    PaymentStatus status,
    CustomerRequestDto buyer
) {

}