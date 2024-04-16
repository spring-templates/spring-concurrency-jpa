package com.concurrency.jpa.customer.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The name of the customer.
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Name {

  /**
   * The first name.
   */
  private String firstName;
  /**
   * The last name.
   */
  private String lastName;
}
