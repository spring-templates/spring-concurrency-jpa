package com.concurrency.jpa.customer.entity;


import com.concurrency.jpa.customer.constant.OrderStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "actual_product")
public class ActualProduct {
    @Id
    @Column(name = "actual_product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", columnDefinition = "varchar(255)")
    private OrderStatus orderStatus;

    @ManyToOne(targetEntity = CoreProduct.class)
    @JoinColumn(name = "core_product_id", nullable = false)
    private CoreProduct coreProduct;

    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "order_price")
    private Long orderPrice;

    @Column(name = "discount_rate")
    private float discountRate;
}
