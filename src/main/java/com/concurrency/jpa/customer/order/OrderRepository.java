package com.concurrency.jpa.customer.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends
        JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Order m join fetch m.actualProducts a join fetch a.coreProduct c where m.paymentId = :id")
    Optional<Order> findByPaymentIdWithFetch(Long id);

    Optional<Order> findByPaymentId(Long id);
}