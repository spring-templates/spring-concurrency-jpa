package com.concurrency.jpa.order;

import org.junit.jupiter.api.Assertions;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문을 생성하고 생성한 주문을 확인하는 테스트")
    public void Order_Create_AND_SELECT_Test(){
        Order order = Order.builder()
                .actor(Actors.Guest)
                .build();
        Order createdOrder = orderRepository.save(order);
        Assertions.assertEquals(order.getId(), orderRepository.findById(createdOrder.getId()).get().getId());
    }
}
