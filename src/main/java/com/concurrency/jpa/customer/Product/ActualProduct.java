package com.concurrency.jpa.customer.Product;


import com.concurrency.jpa.customer.order.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "actual_product")
public class ActualProduct {
    @Id
    @Column(name = "actual_product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "actual_status", columnDefinition = "varchar(255)")
    private OrderStatus actualStatus;

    @ManyToOne(targetEntity = CoreProduct.class)
    @JoinColumn(name = "core_product_id", nullable = false)
    private CoreProduct coreProduct;

    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "order_id")
    private Order order;

    @Getter
    @Column(name = "actual_price")
    private Long actualPrice;

    @Column(name = "discount_rate")
    private float discountRate;
}
