package com.concurrency.jpa.customer.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {
    @ExceptionHandler(value = {BaseException.class, NullPointerException.class})
    public ResponseEntity<?> handlerException( BaseException b){
        b.printStackTrace();
        return ResponseEntity.badRequest().body(new BaseResponse<>(b));
    }
}
