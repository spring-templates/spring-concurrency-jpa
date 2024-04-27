package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import jakarta.persistence.LockModeType;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends
        JpaRepository<Order, Long> {
    @Query(value = "SELECT DISTINCT o FROM Order o JOIN FETCH o.actualProducts WHERE o.paymentId = :id")
    Optional<Order> findByPaymentId(@Param("id") Long id);


}