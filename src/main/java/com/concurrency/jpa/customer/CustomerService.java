package com.concurrency.jpa.customer;

import org.springframework.stereotype.Service;

@Service
public class CustomerService {

  /**
   * Constructor.
   *
   * @param customerRepository the customer repository
   */
  public CustomerService(final CustomerRepository customerRepository) {
  }
}
