package com.concurrency.jpa.customer.order.dto;

import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.enums.PaymentMethods;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    @JsonProperty("actual_products")
    private List<ActualProductDto> actualProducts = new ArrayList<>();
    @JsonProperty("client_type")
    private Actors clientType;
    private Long totalPrice;
    private Long paymentId;
//    @JsonProperty("payment_method")
//    private PaymentMethods paymentMethod;
}
