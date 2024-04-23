package com.concurrency.jpa.customer.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {
    @ExceptionHandler(value = {RuntimeException.class, NullPointerException.class})
    public ResponseEntity<?> handlerException(RuntimeException e, HttpServletRequest request){
        e.printStackTrace();
        return ResponseEntity.badRequest().body(new BaseResponse<>(e));
    }
}
