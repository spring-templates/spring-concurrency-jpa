package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.Product.dto.OrderCoreProductStockDto;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends
        JpaRepository<Order, Long> {

    @Query("select m from Order m join m.actualProducts a where m.paymentId = :id")
    Optional<Order> findByPaymentIdWithFetch(Long id);

    @Query("select new com.concurrency.jpa.customer.Product.dto.OrderCoreProductStockDto(c.id, count(a.id)) " +
            "from Order o join o.actualProducts a join a.coreProduct c where o.id = :id " +
            "group by c.id")
    List<OrderCoreProductStockDto> findCoreProductStockByOrderId(Long id);
    Optional<Order> findByPaymentId(Long id);
}