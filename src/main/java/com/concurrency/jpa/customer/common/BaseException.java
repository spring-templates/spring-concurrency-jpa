package com.concurrency.jpa.customer.common;

import lombok.Getter;

@Getter
public class BaseException  extends RuntimeException{

    private final BaseResponseStatus status;

    public BaseException(BaseResponseStatus status) {
        super(status.getMessage());
        this.printStackTrace();
        this.status = status;
    }

}