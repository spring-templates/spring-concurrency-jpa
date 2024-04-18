package com.concurrency.jpa.customer.order.dto;

import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class ActualProductDto {
    private Long actualProductId;
    private ActualStatus actualStatus;
    private Long coreProductId;
    private Long orderId;
    private Long actualPrice;
    private float discountRate;
}
