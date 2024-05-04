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
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.enums.OrderStatus;
import com.concurrency.jpa.customer.payment.dto.AbstractPayment;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ActualProductRepository actualProductRepository;
    private final CoreProductRepository coreProductRepository;
    private final LockService lockService;
    @Value("${payment.server.url}")
    private String paymentURI;

    @Override
    @Transactional
    public PaymentStatusDto createOrder(CreateOrderRequestDto createOrderRequestDto){
        // 유저 권한 확인하기
        checkUserAuthority(createOrderRequestDto.getClientType());
        return lockService.executeWithLock(createOrderRequestDto.getBuyer().email(),
                1, () -> {
                    // 재고 확인하고 감소시키기
                    updateCoreProductsStock(createOrderRequestDto.getCoreProducts());
                    // 유형제품 찾기
                    List<ActualProduct> actualProducts = concatActualProductList(createOrderRequestDto.getCoreProducts());
                    // 주문 생성
                    // 주문과 유형제품 연결 & 유형제품 상태 업데이트
                    Order savedOrder = getOrder(createOrderRequestDto, actualProducts);
                    PaymentStatusDto payPending = pay(new PaymentInitialRequestDto(
                            AbstractPayment.valueOf(createOrderRequestDto.getPaymentMethod().name()),
                            savedOrder.getTotalPrice(),
                            createOrderRequestDto.getBuyer()));
                    savedOrder.setPaymentId(payPending.paymentId());
                    Order saved = orderRepository.save(savedOrder);
                    return payPending;
                });
    }
    private PaymentStatusDto pay(PaymentInitialRequestDto dto) {
        Mono<PaymentStatusDto> mono = WebClient.create()
                .post()
                .uri(paymentURI+"/payments")
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
    @Transactional
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
        List<ActualProduct> actualProducts = actualProductRepository.findByCoreProduct_IdAndActualStatus(
                coreProductId,
                actualStatus,
                PageRequest.of(0, Math.toIntExact(stock)));
        return actualProducts;
    }

    @Transactional
    public List<ActualProduct> findActualProductsByOrder(Long orderId){
        return actualProductRepository.findByOrder_Id(orderId);
    }

    /**
     * 결제가 실패했기 때문에 해당 결제 id를 가진 주문의 상품들을 이전 상태로 돌려야한다.
     * @param paymentId
     */
    @Override
    @Transactional
    public void rollback(Long paymentId) {
        System.out.println("결제 실패 id : "+paymentId);
        Order order = orderRepository.findByPaymentIdWithFetch(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        order.setOrderStatus(OrderStatus.FAIL);
        System.out.println("주문 id : "+order.getId()+" 주문 상태 : "+order.getOrderStatus());
        // 유형 상품의 상태를 바꾸기
        // 핵심 상품의 재고를 늘리기
        List<ActualProduct> actualProducts = findActualProductsByOrder(order.getId());
        System.out.println("유형 상품 개수 : "+actualProducts.size());
        actualProducts.forEach(
                a -> {
                    a.updateActualProductStatus(ActualStatus.PENDING_ORDER);
                    subtractCoreProductStockPessimistic(a.getCoreProductId(), -1L);
                }
        );
        order.clearActualProducts();
        orderRepository.save(order);
        actualProductRepository.saveAll(actualProducts);
    }
    @Override
    @Transactional
    public void changeActualProductStatus(Long paymentId) {
        Order order = orderRepository.findByPaymentIdWithFetch(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        order.setOrderStatus(OrderStatus.FINISH);
        List<ActualProduct> actualStatusList = findActualProductsByOrder(order.getId());
        actualStatusList.forEach(
                a -> a.updateActualProductStatus(ActualStatus.SHIPPING)
        );
        System.out.println("주문 상태 : "+order.getOrderStatus());
        actualProductRepository.saveAll(actualStatusList);
        orderRepository.save(order);
    }

    @Override
    public OrderDto findByPaymentId(long l) {
        Optional<Order> order = orderRepository.findByPaymentIdWithFetch(l);
        if(order.isEmpty()){
            System.out.println("빈 객체");
            return null;
        }
        else{
            return order.get().toDto();
        }
    }

    /**
     * 요청한 상품의 유형제고가 충분한지 확인
     * @param requireProducts
     */
    @Override
    @Transactional
    public void updateCoreProductsStock(Map<Long, Long> requireProducts) {
        requireProducts.forEach(this::subtractCoreProductStockPessimistic);
    }

    ///////////////////////// 재고량 감소

    @Override
    @Transactional
    public long subtractCoreProductStock(Long coreProductId, Long reqStock){
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        System.out.println("감소 전 핵심 상품 재고량 : "+coreProduct.getStock());
        if(reqStock > coreProduct.getStock()){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional
    public long subtractCoreProductStockPessimistic(Long coreProductId, Long reqStock){
        System.out.println("비관적 상품 id "+coreProductId);
        CoreProduct coreProduct = coreProductRepository.findByIdPessimistic(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        System.out.println("비관적 상품 id "+ coreProduct.getId()+" 감소 전 핵심 상품 재고량 : "+coreProduct.getStock()+" 감소 재고량 : "+reqStock);
        if(reqStock > coreProduct.getStock()){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStockOptimistic(Long coreProductId, Long reqStock) {
        int patience = 0;
        while(true){
            try{
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
            catch(Exception e){
                throw new RuntimeException(e);
            }

        }
    }

}
