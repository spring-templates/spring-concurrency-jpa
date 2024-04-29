package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.enums.OrderStatus;
import com.concurrency.jpa.customer.order.enums.PaymentMethods;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Getter
    private Long totalPrice;

    @Getter
    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private PaymentMethods paymentMethod;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    private OrderStatus orderStatus;

    @Getter
    @OneToMany
    @JoinColumn(name = "order_id")
    private List<ActualProduct> actualProducts = new ArrayList<>();

    // 연관관계 메서드
    public void addActualProducts(List<ActualProduct> actualProducts){
        List<ActualProduct> newActualProducts = new ArrayList<>(actualProducts);
        newActualProducts.forEach(a -> {
            a.updateActualProductStatus(ActualStatus.PROCESSING);
            this.actualProducts.add(a);
            this.totalPrice += a.getActualPrice();
        });
    }

    public OrderDto toDto(){
        return OrderDto.builder()
                .id(id)
                .actualProducts(actualProducts.stream()
                        .map(ActualProduct::toDto)
                        .collect(Collectors.toList()))
                .clientType(actor)
                .totalPrice(totalPrice)
                .paymentMethod(paymentMethod)
                .paymentId(paymentId)
                .orderStatus(orderStatus)
                .build();
    }

    public void setOrderStatus(OrderStatus status){
        this.orderStatus = status;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
}