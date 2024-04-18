package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActualProductRepository extends JpaRepository<ActualProduct, Long>{
    @Query(value = "SELECT count(a.id) FROM ActualProduct a " +
            "WHERE a.coreProduct.id = :coreId AND a.actualStatus = :actualStatus")
    Long countByCoreProductIdANDActualStatus(@Param("coreId") Long k, @Param("actualStatus") ActualStatus actualStatus);
}
