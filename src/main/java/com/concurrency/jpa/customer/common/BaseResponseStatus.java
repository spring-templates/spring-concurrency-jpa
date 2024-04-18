package com.concurrency.jpa.customer.common;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {
    // httpstatus는 code 대신 HttpsStatus 열거형 쓰는게 더 표준적

    SUCCESS(true, HttpStatus.OK, "요청에 성공하였습니다."),
    FAIL(false, HttpStatus.BAD_REQUEST, "요청에 실패했습니다."),
    NOT_ENOUGH_STOCK(false, HttpStatus.BAD_REQUEST, "재고가 충분하지 않습니다.");

    private final boolean isSuccess;
    private final HttpStatus code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, HttpStatus code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
