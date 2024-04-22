package com.concurrency.jpa.customer.Product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "core_product")
public class CoreProduct {
    @Id
    @Getter
    @Column(name = "core_product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long price;
    @Version
    private Long version;
    @Getter
    private Long stock;
    @Column(name = "seller_id")
    private Long sellerId;

    public long addStrock(Long change){
        stock += change;
        return stock;
    }
}
