package com.concurrency.jpa.customer.payment;

import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import com.concurrency.jpa.customer.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PutMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody PaymentStatusDto dto){
        PaymentStatusDto result = paymentService.confirm(dto);
        return ResponseEntity.ok(result);
    }
}
