package com.concurrency.jpa.customer.payment.service;

import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.lock.LockService;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.enums.OrderStatus;
import com.concurrency.jpa.customer.order.service.OrderService;
import com.concurrency.jpa.customer.payment.dto.PaymentStatus;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    @Transactional
    public PaymentStatusDto confirm(PaymentStatusDto dto) throws InterruptedException {
        // 결제 결과를 API로 가져오기
        PaymentStatusDto payResult = getPaymentResult(dto);
        int patience = 4;
        while(patience > 0){
            try{
                return lockService.executeWithLock(dto.buyer().email(),
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
                                orderService.changeActualProductStatus(dto.paymentId());
                                return payResult;
                            }
                            catch (RuntimeException e){
                                cancel(payResult);
                                orderService.rollback(dto.paymentId());
                                throw e;
                            }
                        });
            }
            catch (RuntimeException e){
                e.printStackTrace();
                Thread.sleep(500);
                patience--;
            }
        }
        System.out.println("참을 수 없습니다!!");
        throw new BaseException(BaseResponseStatus.FAIL);
    }

    @Override
    public OrderDto waitUntilFinish(PaymentStatusDto dto) throws InterruptedException {
        int patience = 10;
        while(patience > 0){
            System.out.println("결과 확인 결제 정보 : "+dto);
            try{
                Order order = lockService.executeWithLock(dto.buyer().email(), 1,
                        () ->
                        {
                            Order o = orderRepository.findByPaymentId(dto.paymentId())
                                    .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
                            System.out.println(o.getId()+" : "+o.getOrderStatus());
                            return o;
                        }
                );
                if(order.getOrderStatus()  != OrderStatus.PENDING){
                    return order.toDto(); // lazy loading으로 유형 제품 find
                }
                else{
                    throw new BaseException(BaseResponseStatus.ORDER_COMPLETION_YET);
                }
            }catch (RuntimeException e){
                e.printStackTrace();
                Thread.sleep(1000);
                patience--;
            }
        }
        System.out.println("참을 수 없습니다!!");
        throw new BaseException(BaseResponseStatus.FAIL);
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
