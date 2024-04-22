package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoreProductRepository extends JpaRepository<CoreProduct, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CoreProduct c where c.id = :id" ) // 왜 findByTicketName는 직접 못할까?
    Optional<CoreProduct> findByIdPessimistic(@Param("id") Long id);
}
