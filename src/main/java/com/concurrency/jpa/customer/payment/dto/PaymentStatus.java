package com.concurrency.jpa.customer.payment.dto;

public enum PaymentStatus {
  /**
   * Initial payment status.
   */
  PENDING,
  /**
   * Payment is cancelled.
   */
  CANCELLED,
  /**
   * Payment succeeded via confirmation process.
   */
  SUCCESS,
  /**
   * Payment failed to initialize or confirm.
   */
  @SuppressWarnings("unused") FAILED;

  /**
   * Check if payment is successful.
   *
   * @param result payment status
   * @return true if payment is successful
   */
  public static boolean isSuccess(final PaymentStatusDto result) {
    return SUCCESS.equals(result.status());
  }

  /**
   * Check if payment is cancelled.
   *
   * @param result payment status
   * @return true if payment is cancelled
   */
  public static boolean isCancelled(final PaymentStatusDto result) {
    return CANCELLED.equals(result.status());
  }
}
