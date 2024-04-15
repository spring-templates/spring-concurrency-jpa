package com.concurrency.jpa.customer.entity;

import com.concurrency.jpa.customer.constant.Actors;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDER")
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Actors actor;
    private Long totalPrice;
    private Long totalCount;
    @OneToMany
    private List<ActualProduct> actualProducts = new ArrayList<>();
}