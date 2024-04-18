package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.Product.ActualProduct;
import com.concurrency.jpa.customer.order.enums.Actors;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_table")
public class Order {
    @Id
    @Column(name = "order_id")
    @Getter
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

    // 연관관계 메서드
    public void addActualProduct(ActualProduct actualProduct){
        actualProducts.add(actualProduct);
        totalCount++;
        totalPrice += actualProduct.getActualPrice();
    }
}