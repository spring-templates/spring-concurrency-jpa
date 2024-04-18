package com.concurrency.jpa.customer.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
@Getter
public class BaseResponse<T> {
    // Http Response의 일관성을 높이자

    @JsonProperty("isSuccess") // json 객체 내의 Key 값 설정
    private final Boolean isSuccess;
    private final String message;
    private final HttpStatus code;
    @JsonInclude(JsonInclude.Include.NON_NULL) // json을 만들 때 null인 객체는 제외한다.
    private T result;

    public BaseResponse(T result) {
        // 응답에 성공하고 컨텐츠가 있는 경우, create, update, read 경우
        this.isSuccess = BaseResponseStatus.SUCCESS.isSuccess();
        this.message = BaseResponseStatus.SUCCESS.getMessage();
        this.code = BaseResponseStatus.SUCCESS.getCode();
        this.result = result;
    }

    public BaseResponse(BaseResponseStatus status) {
        // 예외 발생한 경우
        this.isSuccess = status.isSuccess();
        this.message = status.getMessage();
        this.code = status.getCode();
    }
    public BaseResponse(RuntimeException e) {
        // 예외 발생한 경우
        this.isSuccess = BaseResponseStatus.FAIL.isSuccess();
        this.message = e.getMessage();
        this.code = BaseResponseStatus.FAIL.getCode();
    }
}
