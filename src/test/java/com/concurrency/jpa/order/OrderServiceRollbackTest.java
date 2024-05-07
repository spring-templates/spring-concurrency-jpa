package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.lock.LockService;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.enums.OrderStatus;
import com.concurrency.jpa.customer.order.enums.PaymentMethods;
import com.concurrency.jpa.customer.order.service.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderServiceRollbackTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Autowired
    OrderServiceImpl orderService;
    @Autowired
    CoreProductRepository coreProductRepository;
    @Autowired
    ActualProductRepository actualProductRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    LockService lockService;
    static long ACTUAL_STOCK = 0;
    static int threadCount = 10; // 멀티 스레드 개수
    static int requestCount = 3; // 요청 개수
    static long coreProductId = 1L;

    void setup() {
        CoreProduct coreProduct = CoreProduct.builder()
                .id(coreProductId)
                .price((long) 10000)
                .stock(ACTUAL_STOCK)
                .sellerId((long) 1)
                .build();
        CoreProduct savedCoreProduct = coreProductRepository.save(coreProduct);
        List<Order> orders = new ArrayList<>();
        for(int i=1; i<4; i++){
            Order order = Order.builder()
                    .id((long) i)
                    .actor(Actors.InexperiencedCustomer)
                    .paymentId((long) i)
                    .actualProducts(new ArrayList<>())
                    .paymentMethod(PaymentMethods.CREDIT_CARD)
                    .orderStatus(OrderStatus.PENDING)
                    .totalPrice(9000L)
                    .build();
            orders.add(order);
        }
        List<ActualProduct> actualProducts = new ArrayList<>();
        for(int i=1; i<4; i++){
            ActualProduct actualProduct = ActualProduct.builder()
                    .id((long) i)
                    .actualStatus(ActualStatus.PENDING_ORDER)
                    .actualPrice((long) 9000)
                    .discountRate(10)
                    .coreProduct(savedCoreProduct)
                    .build();
            actualProducts.add(actualProduct);
        }
        actualProductRepository.saveAll(actualProducts);
        for(int i=1; i<=3; i++){
            orders.get(i-1).addActualProducts(List.of(actualProducts.get(i-1)));
            actualProducts.get(i-1).setOrder(orders.get(i-1));
        }
        orderRepository.saveAll(orders);
    }

    @Test
    @DisplayName("멀티스레드로 롤백 -> 핵심 상품 재고 롤백 실패")
    public void givenMultiThreadAndTransaction_whenUpdated_thenFAIL() throws InterruptedException {
        setup();
        Long coreProductId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(requestCount);
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        log.info("초기 상품 재고량 : "+coreProduct.getStock());

        for (long i = 1; i <= requestCount; i++) {
            long finalI = i;
            executorService.submit(() -> {
                try {

//                    log.info(order.getPaymentId()+" "+order.getActualProducts().size());
                    orderService.rollback(finalI);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                finally {
                    countDownLatch.countDown();
                } });
        }
        countDownLatch.await();

        Long result = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        List<Order> orders = orderRepository.findAll();
        log.info("핵심 상품 재고 수량 : "+result);
        Assertions.assertNotEquals(ACTUAL_STOCK + requestCount, result);
        for(int i=0; i<3; i++){
            Assertions.assertNotEquals(OrderStatus.FAIL, orders.get(i).getOrderStatus());
        }
    }

    /**
     * 결제 실패를 상정하고 각 주문을 롤백함
     * 1. 주문의 상태를 실패로 수정
     * 2. 유형 제품의 상태를 대기로 수정, 주문 레코드와 연관관계 끊기
     * 3. 핵심 제품의 재고를 늘리기
     * @throws InterruptedException
     */
    @Test
    @DisplayName("멀티스레드로 롤백 -> 핵심 상품 재고 롤백 성공")
    public void givenMultiThreadAndTransaction_whenUpdated_thenSucess() throws InterruptedException {
        setup();
        Long coreProductId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(requestCount);
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        log.info("초기 상품 재고량 : "+coreProduct.getStock());

        for (long i = 1; i <= requestCount; i++) {
            long finalI = i;
            executorService.submit(() -> {
                try {
                    orderService.rollback(finalI);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                finally {
                    countDownLatch.countDown();
                } });
        }
        countDownLatch.await();

        Long result = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        List<Order> orders = orderRepository.findAll();
        log.info("핵심 상품 재고 수량 : "+result);
        Assertions.assertEquals(ACTUAL_STOCK + requestCount, result);
        for(int i=0; i<3; i++){
            Assertions.assertEquals(OrderStatus.FAIL, orders.get(i).getOrderStatus());
        }
    }

    @Test
    @DisplayName("멀티스레드로 롤백 -> 핵심 상품 재고 롤백 성공")
    public void givenMultiThread_Add_coreProduct_thenSucess() throws InterruptedException {
        setup();
        Long coreProductId = 1L;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch countDownLatch = new CountDownLatch(requestCount);
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        log.info("초기 상품 재고량 : "+coreProduct.getStock());

        Map<Long, Long> coreMap = new HashMap<>();
        coreMap.put(1L, -1L);
        for (long i = 1; i <= requestCount; i++) {
            long finalI = i;
            executorService.submit(() -> {
                try {
                    orderService.updateCoreProductsStock(coreMap);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                finally {
                    countDownLatch.countDown();
                } });
        }
        countDownLatch.await();

        Long result = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        List<Order> orders = orderRepository.findAll();
        log.info("핵심 상품 재고 수량 : "+result);
        Assertions.assertEquals(ACTUAL_STOCK + requestCount, result);
    }
}
