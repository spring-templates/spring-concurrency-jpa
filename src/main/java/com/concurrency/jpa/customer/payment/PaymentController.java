package com.concurrency.jpa.customer.payment;

import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import com.concurrency.jpa.customer.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private PaymentService paymentService;

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(PaymentStatusDto dto){
        PaymentStatusDto result = paymentService.confirm(dto);
        return ResponseEntity.ok(result);
    }
}
