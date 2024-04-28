package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActualProductRepository extends JpaRepository<ActualProduct, Long>{
    @Query(value = "SELECT count(a.id) FROM ActualProduct a " +
            "WHERE a.coreProduct.id = :coreId AND a.actualStatus = :actualStatus")
    Long countByCoreProductIdANDActualStatus(@Param("coreId") Long k, @Param("actualStatus") ActualStatus actualStatus);

//    @Query(value = "SELECT a FROM ActualProduct a " +
//            "WHERE a.coreProduct.id = :coreProductId AND a.actualStatus = :reqStatus")
    List<ActualProduct> findByCoreProduct_IdAndActualStatus(@Param("coreProductId") Long coreProductId, @Param("reqStatus")ActualStatus reqStatus, Pageable pageable);

}
