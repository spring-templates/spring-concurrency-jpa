package com.concurrency.jpa.customer.payment.dto;

public enum AbstractPayment {
  /**
   * Payment by credit card.
   */
  @SuppressWarnings("unused") CREDIT_CARD,
  /**
   * Payment by PayPal.
   */
  @SuppressWarnings("unused") PAYPAL,
  /**
   * Payment by iDEAL.
   */
  @SuppressWarnings("unused") IDEAL
}
