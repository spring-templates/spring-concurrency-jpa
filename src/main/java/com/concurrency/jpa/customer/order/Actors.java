package com.concurrency.jpa.customer.order;

public enum Actors {
    Guest(Integer.MAX_VALUE, Integer.MAX_VALUE, false),
    InexperiencedCustomer(1000, 50, true),
    LoyalCustomer(50, 50, true),
    ElderlyCustomer(1000, 1000, true),
    ;

    private int waitOrderMillisec;
    private int waitPaymentMillisec;
    private boolean authority;

    Actors(int waitOrderMillisec, int waitPaymentMillisec, boolean authority) {
        this.waitOrderMillisec = waitOrderMillisec;
        this.waitPaymentMillisec = waitPaymentMillisec;
        this.authority = authority;
    }
}
