package com.concurrency.jpa.customer.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import java.io.Serializable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class Customer implements Serializable {

  /**
   * The email of the customer.
   */
  @EmbeddedId
  private Email email;

  /**
   * The name of the customer.
   */
  @Embedded
  private Name name;
}
