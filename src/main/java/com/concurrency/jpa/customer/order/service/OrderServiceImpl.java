package com.concurrency.jpa.customer.order.service;


import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.ProductService;
import com.concurrency.jpa.customer.Product.dto.OrderCoreProductStockDto;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
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
                    productService.updateCoreProductsStock(createOrderRequestDto.getCoreProducts());
                    // 유형제품 찾기
                    List<ActualProduct> actualProducts = productService.concatActualProductList(createOrderRequestDto.getCoreProducts());
                    // 주문 생성
                    // 주문과 유형제품 연결 & 유형제품 상태 업데이트
                    Order savedOrder = getOrder(createOrderRequestDto, actualProducts);
                    PaymentStatusDto payPending = pay(new PaymentInitialRequestDto(
                            AbstractPayment.valueOf(createOrderRequestDto.getPaymentMethod().name()),
                            savedOrder.getTotalPrice(),
                            createOrderRequestDto.getBuyer()));
                    savedOrder.setPaymentId(payPending.paymentId());
                    orderRepository.save(savedOrder);
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
    public void changeActualProductStatus(Long paymentId) {
        Order order = orderRepository.findByPaymentIdWithFetch(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        order.setOrderStatus(OrderStatus.FINISH);
//        List<ActualProduct> actualStatusList = findActualProductsByOrder(order.getId());
        order.getActualProducts().forEach(
                a -> a.updateActualProductStatus(ActualStatus.SHIPPING)
        );
    }

    /**
     * 결제가 실패했기 때문에 해당 결제 id를 가진 주문의 상품들을 이전 상태로 돌려야한다.
     * 주문, 유형, 핵심 모두 fetch join으로 한번에 가져옴 + 비관적 락
     * => 유형 상품 데이터는 사용하지 않을 건데 패치되어 가져오는건 비효율적이다.
     * DTO Projection을 이용해 필요한 데이터만 가져와서 처리할 수 있다.
     *
     * @param paymentId
     */
    @Override
    @Transactional
    public void rollback(Long paymentId) {
        Order order = orderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        order.setOrderStatus(OrderStatus.FAIL);
        order.getActualProducts().forEach(
                a -> {
                    a.updateActualProductStatus(ActualStatus.PENDING_ORDER);
                }
        );
        Map<Long, Long> coreCntMap = new HashMap<>();
        List<OrderCoreProductStockDto> coreMap = orderRepository.findCoreProductStockByOrderId(order.getId());
        for(OrderCoreProductStockDto o : coreMap){
            System.out.println(o.getCoreProductId()+" : "+o.getReduceStock());
            coreCntMap.put(o.getCoreProductId(), -1*o.getReduceStock());
        }
        productService.updateCoreProductsStock(coreCntMap);
        order.clearActualProducts();
    }

    @Override
    public OrderDto findByPaymentId(long l) {
        Optional<Order> order = orderRepository.findByPaymentIdWithFetch(l);
        if(order.isEmpty()){
            return null;
        }
        else{
            return order.get().toDto();
        }
    }

}
