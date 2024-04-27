package com.concurrency.mock;

import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatus;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Random;

@RestController
public class PaymentMockController {
    private static int paymentId = 1;
    @PostMapping("/mock/payments")
    public ResponseEntity<?> pay(@RequestBody PaymentInitialRequestDto dto){
        PaymentStatusDto result = payService(dto);
        payValidate(dto, result);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/mock/payments/confirm")
    public ResponseEntity<?> confirm(@RequestBody PaymentStatusDto dto){
        PaymentStatusDto result = payConfirm(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/mock/payments/cancel")
    public ResponseEntity<?> cancel(@RequestBody PaymentStatusDto dto){
        return ResponseEntity.ok(new PaymentStatusDto(dto.paymentId(), PaymentStatus.FAILED));
    }

    private PaymentStatusDto payConfirm(PaymentStatusDto dto) {
//        int random = new Random().nextInt(10);
//        if(random < 5){ // 실패하는 경우
//            return new PaymentStatusDto(dto.paymentId(), PaymentStatus.FAILED);
//        }
//        else{
//            return new PaymentStatusDto(dto.paymentId(), PaymentStatus.SUCCESS);
//        }
        return new PaymentStatusDto(dto.paymentId(), PaymentStatus.SUCCESS);
    }

    private void payValidate(PaymentInitialRequestDto request, PaymentStatusDto statusDto) {
        WebClient client = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "*/*")
                .build();

        client.put()
                .uri(request.redirect())
                .bodyValue(statusDto)
                .retrieve()
                .bodyToMono(PaymentStatusDto.class)
                .subscribe(
                        result -> System.out.println("Response: "+result),
                        Throwable::printStackTrace
                );
    }

    private PaymentStatusDto payService(PaymentInitialRequestDto dto) {
        PaymentStatusDto result = new PaymentStatusDto((long)paymentId, PaymentStatus.PENDING);
        paymentId++;
        return result;
    }
}
