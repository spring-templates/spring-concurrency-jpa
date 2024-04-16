package com.concurrency.jpa.customer;

import org.springframework.web.bind.annotation.RestController;

@RestController("/counter")
public class CustomerController {

  /**
   * The Customer service.
   */
  private final CustomerService customerService;

  /**
   * Instantiates a new Customer controller.
   *
   * @param service the service
   */
  public CustomerController(final CustomerService service) {
    this.customerService = service;
  }

}
