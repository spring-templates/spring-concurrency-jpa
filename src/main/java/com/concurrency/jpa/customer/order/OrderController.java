package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.common.BaseResponse;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.service.OrderService;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import com.concurrency.jpa.customer.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class OrderController {
    @Autowired
    OrderService orderService;
    @Autowired
    PaymentService paymentService;

    @PostMapping("/order")
    public ResponseEntity<?> postOrder(@RequestBody CreateOrderRequestDto createOrderRequestDto) {

        PaymentInitialRequestDto paymentRequest = orderService.createOrder(createOrderRequestDto);
        PaymentStatusDto payPending= paymentService.pay(paymentRequest);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setLocation(URI.create("/payment/confirm"));
        // baseResponse에 header 추가
//        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
//                .headers(headers)
//                .body(payPending);
        return ResponseEntity.ok().body(payPending);
    }

    @PutMapping("/confirm")
    public ResponseEntity<?> redirect() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
