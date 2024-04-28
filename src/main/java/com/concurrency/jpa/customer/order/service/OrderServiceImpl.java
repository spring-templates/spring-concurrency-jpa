package com.concurrency.jpa.customer.order.service;


import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.lock.LockService;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.payment.dto.AbstractPayment;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import com.concurrency.jpa.customer.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ActualProductRepository actualProductRepository;
    private final CoreProductRepository coreProductRepository;
    @Value("${payment.server.url}")
    private String paymentURI;
    @Override
    @Transactional
    public PaymentStatusDto createOrder(CreateOrderRequestDto createOrderRequestDto){
        // 유저 권한 확인하기
        checkUserAuthority(createOrderRequestDto.getClientType());
        // 재고 확인하고 감소시키기
        updateCoreProductsStock(createOrderRequestDto.getCoreProducts());
        // 유형제품 찾기
        List<ActualProduct> actualProducts = concatActualProductList(createOrderRequestDto.getCoreProducts());
        // 주문 생성
        // 주문과 유형제품 연결 & 유형제품 상태 업데이트
        Order savedOrder = getOrder(createOrderRequestDto, actualProducts);
        PaymentStatusDto payPending = pay(new PaymentInitialRequestDto(
                AbstractPayment.valueOf(createOrderRequestDto.getPaymentMethod().name()),
                        savedOrder.getTotalPrice()));
        savedOrder.setPaymentId(payPending.paymentId());
        orderRepository.save(savedOrder);
        return payPending;
    }
    private PaymentStatusDto pay(PaymentInitialRequestDto dto) {
        Mono<PaymentStatusDto> mono = WebClient.create()
                .post()
                .uri(paymentURI+"/mock/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PaymentStatusDto.class);
        return mono.block();
    }

    private void checkUserAuthority(Actors clientType) {
        if(clientType.equals(Actors.Guest)){
            throw new BaseException(BaseResponseStatus.NOT_AUTHORITY);
        }
    }

    @Override
    @Transactional
    public Order getOrder(CreateOrderRequestDto createOrderRequestDto, List<ActualProduct> actualProducts) {
        Order order = createOrderRequestDto.toEntity();
        order.addActualProducts(actualProducts);
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActualProduct> concatActualProductList(Map<Long, Long> coreProducts) {
        List<ActualProduct> actualProducts = new ArrayList<>();
        coreProducts.forEach((coreProductId, stock) ->{
                    actualProducts.addAll(
                            findActualProducts(
                                    coreProductId,
                                    ActualStatus.PENDING_ORDER,
                                    stock));
                }

        );
        return actualProducts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActualProduct> findActualProducts(Long coreProductId, ActualStatus actualStatus, Long stock){
        System.out.println("유형 상품 찾기 전");
        return actualProductRepository.findByCoreProduct_IdAndActualStatus(
                coreProductId,
                actualStatus,
                PageRequest.of(0, Math.toIntExact(stock)));
    }

    /**
     * 요청한 상품의 유형제고가 충분한지 확인
     * @param requireProducts
     */
    @Override
    @Transactional
    public void updateCoreProductsStock(Map<Long, Long> requireProducts) {
        requireProducts.forEach(this::subtractCoreProductStock);
    }

    /**
     * 결제가 실패했기 때문에 해당 결제 id를 가진 주문의 상품들을 이전 상태로 돌려야한다.
     * @param paymentId
     */
    @Transactional
    public void rollback(Long paymentId) {
        System.out.println("결제 실패 id : "+paymentId);
        Order order = orderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        System.out.println("주문 id : "+order.getId());
        // 유형 상품의 상태를 바꾸기
        // 핵심 상품의 재고를 늘리기
        order.getActualProducts().forEach(
                a -> {
                    System.out.println("롤백 상품 : "+a.getId()+" 핵심 id : "+a.getCoreProductId());
                    a.updateActualProductStatus(ActualStatus.PENDING_ORDER);
                    subtractCoreProductStock(a.getCoreProductId(), -1L);
                }
        );
    }

    ///////////////////////// 재고량 감소

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStock(Long coreProductId, Long reqStock){
        System.out.println("핵심 상품 찾기 전 : "+coreProductId);
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        if(reqStock > coreProduct.getStock()){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStockPessimistic(Long coreProductId, Long reqStock){
        CoreProduct coreProduct = coreProductRepository.findByIdPessimistic(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        if(reqStock > coreProduct.getStock()){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStockOptimistic(Long coreProductId, Long reqStock) throws InterruptedException {
        int patience = 0;
        while(true){
            try{
                CoreProduct coreProduct = coreProductRepository.findByIdPessimistic(coreProductId)
                        .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
                if(reqStock > coreProduct.getStock()){
                    throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
                }
                return coreProduct.addStrock(-reqStock);
            }
            catch(Exception oe){
                if(patience == 10){
                    throw new BaseException(BaseResponseStatus.OPTIMISTIC_FAILURE);
                }
                System.out.println("현재까지 "+patience+"번 참음");
                patience++;
                Thread.sleep(500);
            }
        }
    }

}
