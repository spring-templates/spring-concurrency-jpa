package com.concurrency.jpa.customer.payment.dto;

public record CustomerRequestDto(
    String email,
    String name
) {

}