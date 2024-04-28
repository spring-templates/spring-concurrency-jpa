package com.concurrency.jpa.customer.payment.service;

import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.service.OrderService;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatus;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.net.URI;

/**
 * 외부 결제 서버에 결제 요청, 결과 확인, 결제 취소를 요청하는 부분
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService{

    @Value("${payment.server.url}")
    private String paymentURI;

    private final OrderRepository orderRepository;
    private final OrderService orderService;
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
        // 결제 결과를 가져오기
        PaymentStatusDto payResult = getPaymentResult(dto);
        System.out.println("결제 결과 : "+payResult);
        if(payResult.status().equals(PaymentStatus.FAILED)){
            // 결제 실패한 경우
            // 분산락을 사용했다면 예외를 발생시켰을 때 주문 속 상품의 상태가 PROCESSING에서 PENDING으로 바뀌어야함.
            Order order = orderRepository.findByPaymentId(dto.paymentId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
            System.out.println("롤백 대상 주문 id : "+order.getId());
            orderService.rollback(dto.paymentId());
            // Runtime exception을 발생시키면 롤백한걸 롤백하게된다.
            throw new BaseException(BaseResponseStatus.PAYMENT_FAILED);
        }
        try{
            // 결제 성공한 경우
            // 결제 id를 가진 주문을 찾기
            System.out.println("결제 id가 "+dto.paymentId()+"를 가진 주문을 찾자.");
            Order order = orderRepository.findByPaymentId(dto.paymentId())
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
            System.out.println("주문 번호 : "+order.getId());
            // 해당 주문에 들어간 상품의 상태를 바꾸기
            changeActualProductStatus(order);
            return payResult;
        }
        catch (RuntimeException e){
            cancel(payResult);
            orderService.rollback(dto.paymentId());
            // 분산락을 사용했다면 주문 속 상품의 상태가 PENDING으로 바뀌어야함.
            throw e;
        }

    }



    @Override
    public OrderDto result(PaymentStatusDto dto) {
        System.out.println("결과 확인 결제 정보 : "+dto);
        Order order = orderRepository.findByPaymentId(dto.paymentId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        return order.toDto();
    }

    private PaymentStatusDto getPaymentResult(PaymentStatusDto dto) {
        Mono<PaymentStatusDto> mono = WebClient.create()
                .put()
                .uri(getUri("/mock/payments/confirm"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PaymentStatusDto.class);
        return mono.block();
    }

    @Transactional
    public void changeActualProductStatus(Order order) {
        order.getActualProducts()
                .forEach(ap -> ap.updateActualProductStatus(ActualStatus.SHIPPING));
    }

    @Override
    public PaymentStatusDto cancel(PaymentStatusDto dto) {
        Mono<PaymentStatusDto> mono = WebClient.create()
                .post()
                .uri(getUri("/mock/payments/cancel"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(PaymentStatusDto.class);
        return mono.block();
    }
}
