package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.service.OrderService;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import com.concurrency.jpa.customer.payment.service.PaymentService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class OrderController {
    @Autowired
    OrderService orderService;


    @PostMapping("/order")
    public ResponseEntity<?> postOrder(@RequestBody CreateOrderRequestDto createOrderRequestDto) {

        PaymentStatusDto paymentRequest = orderService.createOrder(createOrderRequestDto);

        // 사용자가 결제 결과 화면 볼 수 있도록 리다이렉트
        URI redirectUri = UriComponentsBuilder.fromUriString("/payments/result")
                .queryParam("paymentId", paymentRequest.paymentId())
                .queryParam("status", paymentRequest.status())
                .build()
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        System.out.println(paymentRequest);
        // 사용자가 결제 결과 화면 볼 수 있도록 리다이렉트
        headers.setLocation(redirectUri);
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @PutMapping("/confirm")
    public ResponseEntity<?> redirect() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @GetMapping("/payment/id")
    public ResponseEntity<?> getPayment() {
        OrderDto dto = orderService.findByPaymentId(3L);
        return ResponseEntity.ok(dto);
    }
}
