package com.concurrency.jpa.customer.payment;

import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatus;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import com.concurrency.jpa.customer.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 주문 서버가 결제 서버에게 결제 결과를 요청함
     * @param dto
     * @return
     */
    @PutMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody PaymentStatusDto dto){
        System.out.println("결제 서버로 부터 받은 정보 : "+dto);
        PaymentStatusDto result = null;
        try{
            result = paymentService.confirm(dto);
        }
        catch (BaseException | InterruptedException e){
            throw new RuntimeException(e);
        }

        System.out.println("결제 서버로 보낼 정보 : "+result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/result")
    public ResponseEntity<?> result(@RequestParam("paymentId") Long paymentId,
                                    @RequestParam("status") PaymentStatus status){
        PaymentStatusDto dto = new PaymentStatusDto(paymentId, status);
        try{
            OrderDto result = paymentService.result(dto);
            return ResponseEntity.ok(result);
        }
        catch (BaseException | InterruptedException e){
            throw new RuntimeException(e);
        }

    }
}
