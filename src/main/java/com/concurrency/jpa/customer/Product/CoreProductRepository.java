package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.dto.StockDto;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CoreProductRepository extends JpaRepository<CoreProduct, Long> {
}
