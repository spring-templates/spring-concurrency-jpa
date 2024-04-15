package com.concurrency.jpa.customer.entity;


import com.concurrency.jpa.customer.constant.OrderStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "actual_product")
public class ActualProduct {
    @Id
    @Column(name = "actual_product_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private OrderStatus orderStatus;

    @ManyToOne(targetEntity = CoreProduct.class)
    @JoinColumn(name = "core_product_id")
    private CoreProduct coreProduct;

    @Column(name = "order_price")
    private Long orderPrice;

    @Column(name = "discount_rete")
    private float discountRate;
}
