package com.concurrency.jpa.customer.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "core_product")
public class CoreProduct {
    @Id
    @Column(name = "core_product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long price;
    private Long stock;
    @Column(name = "seller_id")
    private String sellerId;
}
