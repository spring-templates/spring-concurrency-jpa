package com.concurrency.jpa.customer.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "core_product")
public class CoreProduct {
    @Id
    @Column(name = "core_product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long price;
    private Long stock;
    @Column(name = "seller_id")
    private Long sellerId;
}
