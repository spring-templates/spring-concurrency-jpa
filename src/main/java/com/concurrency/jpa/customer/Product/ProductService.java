package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;

import java.util.List;
import java.util.Map;

public interface ProductService {
    List<ActualProduct> findActualProductsByOrder(Long orderId);
    List<ActualProduct> findActualProducts(Long coreProductId, ActualStatus actualStatus, Long stock);

    List<ActualProduct> concatActualProductList(Map<Long, Long> coreProducts);
    void updateCoreProductsStock(Map<Long, Long> requireProducts);
    long subtractCoreProductStock(Long coreProductId, Long reqStock);
    long subtractCoreProductStockPessimistic(Long coreProductId, Long reqStock);
    long subtractCoreProductStockOptimistic(Long coreProductId, Long reqStock);
}
