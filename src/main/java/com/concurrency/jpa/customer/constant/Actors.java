package com.concurrency.jpa.customer.constant;

public enum Actors {
    Guest(Integer.MAX_VALUE, Integer.MAX_VALUE),
    InexperiencedCustomer(1000, 50),
    LoyalCustomer(50, 50),
    ElderlyCustomer(1000, 1000),
    ;

    private int waitOrderMillisec;
    private int waitPaymentMillisec;

    Actors(int waitOrderMillisec, int waitPaymentMillisec) {
        this.waitOrderMillisec = waitOrderMillisec;
        this.waitPaymentMillisec = waitPaymentMillisec;
    }
}
