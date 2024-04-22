package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.order.service.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SpringBootTest
public class ConcurrentOrderServiceTest {

    @Autowired
    OrderServiceImpl orderService;
    @Autowired
    CoreProductRepository coreProductRepository;
    static int ACTUAL_STOCK = 30;
    static int threadCount = 10; // 멀티 스레드 개수
    static int requestCount = 30; // 요청 개수

    @BeforeEach
    void setup() {
        CoreProduct coreProduct = CoreProduct.builder()
                .id((long)1)
                .price((long) 10000)
                .stock((long) ACTUAL_STOCK)
                .sellerId((long) 1)
                .version((long) 1)
                .build();
        coreProductRepository.save(coreProduct);
    }


    @Test
//    @Transactional
    @DisplayName("싱글스레드로 재고 감소 후 체크")
    public void Update_CoreStock_Success(){
        // given
        CoreProduct coreProduct1 = coreProductRepository.findById(1L).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        System.out.println("시작 상품 재고 : "+coreProduct1.getStock());

        // when
        orderService.subtractCoreProductStock(1L, 1L);
//        coreProductRepository.save(coreProduct1);

        // then
        CoreProduct coreProduct2 = coreProductRepository.findById(1L).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        System.out.println("나중 상품 재고 : "+coreProduct2.getStock());
        Assertions.assertEquals(ACTUAL_STOCK - 1L, coreProduct2.getStock());
    }

    @Test
    @DisplayName("멀티스레드로 재고 감소, 낙관적 락으로 경쟁 상태 관리.")
    public void givenMultiThreadAndTransaction_whenUpdated_thenFAIL() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 스레드는 countDown을 호출해서 requestCount를 하나씩 감소시킴
        CountDownLatch countDownLatch = new CountDownLatch(requestCount);
        CoreProduct coreProduct = coreProductRepository.findById(1L).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        System.out.println("초기 상품 재고량 : "+coreProduct.getStock());

        for (int i = 0; i < requestCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.subtractCoreProductStock(1L, 1L);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                finally {
                    countDownLatch.countDown();
                } });
        }
        countDownLatch.await();

        Long result = coreProductRepository.findById(1L).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        System.out.println("티켓 수량 : "+result);
        Assertions.assertEquals(ACTUAL_STOCK - requestCount, result);
    }

    @Test
    @DisplayName("멀티스레드로 재고 감소, 비관적 락으로 경쟁 상태 제거")
    public void givenMultiThreadAndTransaction_whenUpdated_thenSucess() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // 스레드는 countDown을 호출해서 requestCount를 하나씩 감소시킴
        CountDownLatch countDownLatch = new CountDownLatch(requestCount);
        CoreProduct coreProduct = coreProductRepository.findById(1L).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));
        System.out.println("초기 상품 재고량 : "+coreProduct.getStock());

        for (int i = 0; i < requestCount; i++) { // wating time일 수 있다.
            executorService.submit(() -> { // submit 안에 함수는 스레드가 실행시킴
                try {
                    orderService.subtractCoreProductStockPessimistic(1L, 1L); // 티켓 수량 감소
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                finally {
                    countDownLatch.countDown();
                } });
        }
        // 메인 스레드는 requestCount가 0이 될때까지 blocked된다.
        countDownLatch.await();

        Long result = coreProductRepository.findById(1L).orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다.")).getStock();
        System.out.println("티켓 수량 : "+result);
        Assertions.assertEquals(0L, result);
    }
}
