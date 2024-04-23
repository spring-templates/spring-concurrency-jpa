package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.common.BaseResponse;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.order.service.OrderService;
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

    @PostMapping("/order")
    public ResponseEntity<?> postOrder(@RequestBody CreateOrderRequestDto createOrderRequestDto) {

        OrderDto orderDto = orderService.createOrder(createOrderRequestDto);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/confirm"));
        // baseResponse에 header 추가
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .headers(headers)
                .body(orderDto);
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> redirect() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
