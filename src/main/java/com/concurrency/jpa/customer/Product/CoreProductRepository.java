package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoreProductRepository extends JpaRepository<CoreProduct, Long> {
}
