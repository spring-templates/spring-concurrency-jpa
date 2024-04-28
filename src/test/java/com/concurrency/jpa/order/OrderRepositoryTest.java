package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.order.enums.PaymentMethods;
import org.junit.jupiter.api.Assertions;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Test
    @Transactional
    @DisplayName("주문을 생성하고 생성한 주문에 결제를 넣는 테스트")
    public void Order_Create_AND_Payment_test(){
        Order order = Order.builder()
                .actor(Actors.Guest)
                .paymentMethod(PaymentMethods.CREDIT_CARD)
                .build();
        order.setPaymentId(15L);
        Order createdOrder = orderRepository.save(order);
        List<Order> orderList = orderRepository.findAll();
        orderList.forEach(
                o -> {
                    System.out.println("id : "+o.getId()+" 결제 id : "+o.getPaymentId());
                }
        );
        Order findOrder = orderRepository.findByPaymentId(15L)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        Assertions.assertEquals(createdOrder.getId(), findOrder.getId());
    }
}
