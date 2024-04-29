package com.concurrency.jpa.customer.payment.service;

import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.lock.LockService;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.enums.OrderStatus;
import com.concurrency.jpa.customer.order.service.OrderService;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatus;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.util.List;

/**
 * 외부 결제 서버에 결제 요청, 결과 확인, 결제 취소를 요청하는 부분
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    @Value("${payment.server.url}")
    private String paymentURI;

    private final OrderRepository orderRepository;
    private final ActualProductRepository actualProductRepository;
    private final OrderService orderService;
    private final LockService lockService;
    private URI getUri(String uri) {
        return URI.create(paymentURI+uri);
    }

    /**
     * 결제 성공 시 수행하는 로직
     * 1. 결제 번호로 주문 찾기
     * 2. 주문에 포함된 상품의 상태를 배송 중으로 수정
     * 3.
     * @param dto
     * @return
     */
    @Override
    public PaymentStatusDto confirm(PaymentStatusDto dto) {
        // 결제 결과를 API로 가져오기
        PaymentStatusDto payResult = getPaymentResult(dto);
        return lockService.executeWithLock(dto.paymentId(),
                1, () -> {
                    if(payResult.status().equals(PaymentStatus.FAILED)){
                        System.out.println("결제 결과 : "+payResult);
                        // 결제 실패한 경우
                        orderService.rollback(dto.paymentId());
                        return payResult;
                    }
                    try{
                        // 결제 성공한 경우
                        // 결제 id를 가진 주문을 찾기
                        System.out.println("결제 id가 "+dto.paymentId()+"를 가진 주문을 찾자.");
                        // 해당 주문에 들어간 상품의 상태를 바꾸기
                        changeActualProductStatus(dto.paymentId());
                        return payResult;
                    }
                    catch (RuntimeException e){
                        cancel(payResult);
                        orderService.rollback(dto.paymentId());
                        throw e;
                    }
                });
    }


    @Override
    public OrderDto result(PaymentStatusDto dto) throws InterruptedException {
        System.out.println("");
        int patience = 4;
//        while(patience > 0){
//            System.out.println("결과 확인 결제 정보 : "+dto);
//            Order order = null;
//            try{
//                order = lockService.executeWithLock(dto.paymentId(), 1,
//                        () ->
//                        {
//                            Order o = orderRepository.findByPaymentId(dto.paymentId())
//                                    .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
//                            System.out.println(o.getId()+" : "+o.getOrderStatus());
//                            return o;
//                        }
//                );
//            }catch (RuntimeException e){
//                e.printStackTrace();
//            }
//            if(order != null && order.getOrderStatus() != OrderStatus.PENDING){
//                return order.toDto();
//            }
//            Thread.sleep(500);
//            patience--;
//        }
//        System.out.println("참을 수 없습니다!!");
//        throw new BaseException(BaseResponseStatus.FAIL);
        return null;
    }

    private PaymentStatusDto getPaymentResult(PaymentStatusDto dto) {
        Mono<PaymentStatusDto> mono = WebClient.create()
                .put()
                .uri(getUri("/payments/confirm"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PaymentStatusDto.class);
        return mono.block();
    }

    @Transactional
    public void changeActualProductStatus(Long paymentId) {
        Order order = orderRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        order.setOrderStatus(OrderStatus.FINISH);
        List<ActualProduct> actualStatusList = orderService.findActualProductsByOrder(order.getId());
        actualStatusList.forEach(
                a -> a.updateActualProductStatus(ActualStatus.SHIPPING)
        );
        System.out.println("주문 상태 : "+order.getOrderStatus());
        actualProductRepository.saveAll(actualStatusList);
        orderRepository.save(order);
    }

    @Override
    public PaymentStatusDto cancel(PaymentStatusDto dto) {
        Mono<PaymentStatusDto> mono = WebClient.create()
                .post()
                .uri(getUri("/payments/cancel"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PaymentStatusDto.class);
        return mono.block();
    }
}
