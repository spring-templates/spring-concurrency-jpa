package com.concurrency.jpa.customer.Product.dto;

import lombok.Getter;
import lombok.Value;

@Value
@Getter
public class StockDto {
    Long stock;
}
