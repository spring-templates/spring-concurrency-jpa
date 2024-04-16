package com.concurrency.jpa.customer.entity;

import com.concurrency.jpa.customer.constant.Actors;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_table")
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private Actors actor;
    private Long totalPrice;
    private Long totalCount;

    @OneToMany
    @JoinColumn(name = "order_id")
    private List<ActualProduct> actualProducts = new ArrayList<>();
}