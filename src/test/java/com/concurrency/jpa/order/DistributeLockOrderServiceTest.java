package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.lock.LockService;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.service.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DistributeLockOrderServiceTest {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Autowired
    OrderServiceImpl orderService;
    @Autowired
    CoreProductRepository coreProductRepository;
    @Autowired
    LockService lockService;
    static int ACTUAL_STOCK = 30;
    static int threadCount = 10; // 멀티 스레드 개수
    static int requestCount = 30; // 요청 개수

    @BeforeAll
    void setup() {
        CoreProduct coreProduct1 = CoreProduct.builder()
                .id((long)1)
                .price((long) 10000)
                .stock((long) ACTUAL_STOCK)
                .sellerId((long) 1)
                .version((long) 1)
                .build();
        coreProductRepository.save(coreProduct1);
        CoreProduct coreProduct2 = CoreProduct.builder()
                .id((long)2)
                .price((long) 10000)
                .stock((long) ACTUAL_STOCK)
                .sellerId((long) 1)
                .version((long) 1)
                .build();
        coreProductRepository.save(coreProduct2);
        CoreProduct coreProduct3 = CoreProduct.builder()
                .id((long)3)
                .price((long) 10000)
                .stock((long) ACTUAL_STOCK)
                .sellerId((long) 1)
                .version((long) 1)
                .build();
        coreProductRepository.save(coreProduct3);
        CoreProduct coreProduct4 = CoreProduct.builder()
                .id((long)4)
                .price((long) 10000)
                .stock((long) ACTUAL_STOCK)
                .sellerId((long) 1)
                .version((long) 1)
                .build();
        coreProductRepository.save(coreProduct4);
    }


    @Test
    @DisplayName("싱글스레드로 재고 감소 후 체크")
    public void Update_CoreStock_Success(){
        Long coreProductId = 1L;
        // given
        CoreProduct coreProduct1 = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        System.out.println("시작 상품 재고 : "+coreProduct1.getStock());

        // when
        orderService.subtractCoreProductStock(coreProductId, 1L);

        // then
        CoreProduct coreProduct2 = coreProductRepository.findById(coreProductId).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        System.out.println("나중 상품 재고 : "+coreProduct2.getStock());
        Assertions.assertEquals(ACTUAL_STOCK - 1L, coreProduct2.getStock());
    }

    @Test
    @DisplayName("멀티스레드로 재고 감소 후 체크, 분산락을 이용해서 동시성 제어")
    public void GivenDistributeLock_Update_CoreStock_Success() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        Long coreProductId = 1L;
        Runnable lockThreadOne = () -> {
            UUID uuid = UUID.randomUUID();
            log.info("task start thread: " + uuid);
            try {
                lockService.executeWithLock("user@naver.com",
                        1,
                        ()-> orderService.subtractCoreProductStock(coreProductId, 1L));
            }
            catch (Exception e0) {
                e0.printStackTrace();
                log.info("exception thrown with thread: " + uuid);
                throw e0;
            }
            finally {
                latch.countDown();
            }
        };

        Runnable lockThreadTwo = () -> {
            UUID uuid = UUID.randomUUID();
            log.info("task start thread: " + uuid);
            try {
                lockService.executeWithLock("user@naver.com",
                        1,
                        ()-> orderService.subtractCoreProductStock(coreProductId, 1L));
            }catch (Exception e0) {
                e0.printStackTrace();
                log.info("exception thrown with thread: " + uuid);
                throw e0;
            }finally {
                latch.countDown();
            }
        };
        executorService.submit(lockThreadOne);
        executorService.submit(lockThreadTwo);
        latch.await();
        Long result = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        log.info("티켓 수량 : "+result);
        Assertions.assertEquals(ACTUAL_STOCK-1, result);
    }

    @Test
    @DisplayName("분산락을 서로 다른 쓰레드가 획득할 수 있는지 체크")
    public void GivenDistributeLock_Share_Other_Success() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch1 = new CountDownLatch(1);
        Long coreProductId = 1L;
        Runnable lockThreadOne = () -> {
            UUID uuid = UUID.randomUUID();
            log.info("task start thread: " + uuid);
            try {
                lockService.executeWithLock("user@naver.com",
                        1,
                        ()-> orderService.subtractCoreProductStock(coreProductId, 1L));
            }
            catch (Exception e0) {
                e0.printStackTrace();
                log.info("exception thrown with thread: " + uuid);
                throw e0;
            }
            finally {
                latch1.countDown();
            }
        };
        executorService.submit(lockThreadOne);
        latch1.await();

        CountDownLatch latch2 = new CountDownLatch(1);
        Runnable lockThreadTwo = () -> {
            UUID uuid = UUID.randomUUID();
            log.info("task start thread: " + uuid);
            try {
                lockService.executeWithLock("user@naver.com",
                        1,
                        ()-> orderService.subtractCoreProductStock(coreProductId, 1L));
            }catch (Exception e0) {
                e0.printStackTrace();
                log.info("exception thrown with thread: " + uuid);
                throw e0;
            }finally {
                latch2.countDown();
            }
        };
        executorService.submit(lockThreadTwo);
        latch2.await();
        Long result = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        log.info("티켓 수량 : "+result);
        Assertions.assertEquals(ACTUAL_STOCK-2, result);
    }

    @Test
    @DisplayName("트랜젝션이 끝나고 분산락을 해제하기 전에 예외가 발생한다면 트랜젝션도 롤백되지 않음")
    public void GivenDistributeLock_Tx_Finished_But_exception() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch1 = new CountDownLatch(1);
        Long coreProductId = 1L;
        Runnable lockThreadOne = () -> {
            UUID uuid = UUID.randomUUID();
            log.info("task start thread: " + uuid);
            try {
                lockService.executeWithLock("user@naver.com",
                        1,
                        ()-> {
                            orderService.subtractCoreProductStock(coreProductId, 1L);
                            throw new RuntimeException("강제 예외 발생");
                        });
            }
            catch (Exception e0) {
                e0.printStackTrace();
                log.info("exception thrown with thread: " + uuid);
                throw e0;
            }
            finally {
                latch1.countDown();
            }
        };
        executorService.submit(lockThreadOne);
        latch1.await();
        Long result = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        log.info("티켓 수량 : "+result);
        Assertions.assertEquals(ACTUAL_STOCK-1, result);
    }

    @Test
    @DisplayName("연관관계를 업데이트하고 결과를 읽기")
    public void Relation_Update() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch1 = new CountDownLatch(1);
        Long coreProductId = 1L;
        Runnable lockThreadOne = () -> {
            UUID uuid = UUID.randomUUID();
            log.info("task start thread: " + uuid);
            try {
                lockService.executeWithLock("user@naver.com",
                        1,
                        ()-> {
                            orderService.subtractCoreProductStock(coreProductId, 1L);
                            throw new RuntimeException("강제 예외 발생");
                        });
            }
            catch (Exception e0) {
                e0.printStackTrace();
                log.info("exception thrown with thread: " + uuid);
                throw e0;
            }
            finally {
                latch1.countDown();
            }
        };
        executorService.submit(lockThreadOne);
        latch1.await();
        Long result = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        log.info("티켓 수량 : "+result);
        Assertions.assertEquals(ACTUAL_STOCK-1, result);
    }
}
