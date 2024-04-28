package com.concurrency.jpa.lock;

import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.lock.LockService;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.enums.PaymentMethods;
import com.concurrency.jpa.customer.order.service.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.locks.LockRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DistributeLockTest {
    @Autowired
    OrderServiceImpl orderService;
    @Autowired
    CoreProductRepository coreProductRepository;
    @Autowired
    ActualProductRepository actualProductRepository;
    @Autowired
    LockService lockService;
    LockRegistry lockRegistry;
    private static final String MY_LOCK_KEY = "someLockKey";

    @BeforeAll
    void setup() {
        CoreProduct coreProduct = CoreProduct.builder()
                .id((long)1)
                .price((long) 10000)
                .stock((long) 3)
                .sellerId((long) 1)
                .version((long) 1)
                .build();
        coreProductRepository.save(coreProduct);
        List<ActualProduct> actualProducts;
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
        actualProductRepository.saveAll(actualProducts);
    }

    @Test
    @DisplayName("두 쓰레드가 같은 락을 차지하도록 유도 -> 한쪽 스레드는 락을 차지하지 못함")
    public void Update_CoreStock_Success(){
        var executor = Executors.newFixedThreadPool(2);
        Map<Long, Long> coreProduct = new HashMap<>();
        coreProduct.put((long) 1,(long) 2);
        CreateOrderRequestDto createOrderRequestDto = new CreateOrderRequestDto(coreProduct, Actors.InexperiencedCustomer, PaymentMethods.CREDIT_CARD);
        Runnable lockThreadOne = () -> {
            UUID uuid = UUID.randomUUID();
            try {
                lockService.executeWithLock(createOrderRequestDto.getClientType().name(),
                        1,
                        ()->orderService.createOrder(createOrderRequestDto));
            }
            catch (Exception e0) {
                e0.printStackTrace();
                System.out.println("exception thrown with thread: " + uuid);
                throw e0;
            }
        };

        Runnable lockThreadTwo = () -> {
            UUID uuid = UUID.randomUUID();
            try {
                lockService.executeWithLock(createOrderRequestDto.getClientType().name(),
                        1,
                        ()->orderService.createOrder(createOrderRequestDto));
            }catch (Exception e0) {
                e0.printStackTrace();
                System.out.println("exception thrown with thread: " + uuid);
                throw e0;
            }
        };
        executor.submit(lockThreadOne);
        executor.submit(lockThreadTwo);
        executor.shutdown();
//        Assertions.assertThrows(BaseException.class, executor::shutdown);
    }




}
