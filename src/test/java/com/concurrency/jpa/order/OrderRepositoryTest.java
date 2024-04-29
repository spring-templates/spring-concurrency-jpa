package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.order.enums.OrderStatus;
import com.concurrency.jpa.customer.order.enums.PaymentMethods;
import org.junit.jupiter.api.Assertions;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;
    private List<ActualProduct> actualProducts = new ArrayList<>();
    private CoreProduct coreProduct;
    @BeforeEach
    void setup() {
        coreProduct = CoreProduct.builder()
                .id((long)1)
                .price((long) 10000)
                .stock((long) 3)
                .sellerId((long) 1)
                .build();
        ActualProduct ap1 = ActualProduct.builder()
                .id((long) 1)
                .actualStatus(ActualStatus.PENDING_ORDER)
                .actualPrice((long) 9000)
                .discountRate(10)
                .coreProduct(coreProduct)
                .build();
        ActualProduct ap2 = ActualProduct.builder()
                .id((long) 2)
                .actualStatus(ActualStatus.PENDING_ORDER)
                .actualPrice((long) 8000)
                .discountRate(20)
                .coreProduct(coreProduct)
                .build();
        ActualProduct ap3 = ActualProduct.builder()
                .id((long) 3)
                .actualStatus(ActualStatus.PENDING_ORDER)
                .actualPrice((long) 7000)
                .discountRate(30)
                .coreProduct(coreProduct)
                .build();
        actualProducts = List.of(ap1, ap2, ap3);
    }
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
        Long paymendId = 35L;
        Order order = Order.builder()
                .actor(Actors.Guest)
                .actualProducts(new ArrayList<>())
                .totalPrice(0L)
                .orderStatus(OrderStatus.PENDING)
                .paymentMethod(PaymentMethods.CREDIT_CARD)
                .build();
        order.addActualProducts(actualProducts);
        order.setPaymentId(paymendId);
        Order createdOrder = orderRepository.save(order);
        Order findOrder = orderRepository.findByPaymentId(paymendId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        Assertions.assertEquals(createdOrder.getId(), findOrder.getId());
    }
}
