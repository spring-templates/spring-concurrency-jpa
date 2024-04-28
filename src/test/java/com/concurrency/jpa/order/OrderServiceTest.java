package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.service.OrderService;
import com.concurrency.jpa.customer.order.service.OrderServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @InjectMocks
    OrderServiceImpl orderService;
    @Mock
    ActualProductRepository actualProductRepository;
    @Mock
    CoreProductRepository coreProductRepository;
    private List<ActualProduct> actualProducts;
    private CoreProduct coreProduct;
    @BeforeEach
    void setup() {
        coreProduct = CoreProduct.builder()
                .id((long)1)
                .price((long) 10000)
                .stock((long) 3)
                .sellerId((long) 1)
                .build();
        ActualProduct ap1 = ActualProduct.builder()
                .id((long) 1)
                .actualStatus(ActualStatus.PENDING_ORDER)
                .actualPrice((long) 9000)
                .discountRate(10)
                .coreProduct(coreProduct)
                .build();
        ActualProduct ap2 = ActualProduct.builder()
                .id((long) 2)
                .actualStatus(ActualStatus.PENDING_ORDER)
                .actualPrice((long) 8000)
                .discountRate(20)
                .coreProduct(coreProduct)
                .build();
        ActualProduct ap3 = ActualProduct.builder()
                .id((long) 3)
                .actualStatus(ActualStatus.PENDING_ORDER)
                .actualPrice((long) 7000)
                .discountRate(30)
                .coreProduct(coreProduct)
                .build();
        actualProducts = List.of(ap1, ap2, ap3);
    }

    @Test
    public void Update_CoreStock_Success(){
        when(coreProductRepository.findById((long) 1))
                .thenReturn(Optional.of(coreProduct));
        Assertions.assertEquals(1, orderService.subtractCoreProductStock((long) 1, (long) 2));
    }

    @Test
    public void Update_CoreStock_Fail(){
        when(coreProductRepository.findById((long) 1))
                .thenReturn(Optional.of(coreProduct));
        Exception exception = Assertions.assertThrows(BaseException.class,
                () -> orderService.subtractCoreProductStock((long) 1, (long) 4));

        Assertions.assertTrue(exception.getMessage().contains("재고가 충분하지 않습니다."));
    }

    @Test
    public void Find_ProductStock_Success(){
        when(actualProductRepository.findByCoreProduct_IdAndActualStatus(
                (long) 1,
                ActualStatus.PENDING_ORDER,
                PageRequest.of(0, Math.toIntExact(3))))
                .thenReturn(actualProducts);
        Map<Long, Long> requireStock = new HashMap<>();
        requireStock.put((long) 1, (long) 3);
        Assertions.assertEquals(3, orderService.findActualProducts(
                (long) 1,
                ActualStatus.PENDING_ORDER,
                (long) 3
        ).size());
    }

    @Test
    public void shouldProductStockException(){
        Map<Long, Long> requireStock = new HashMap<>();
        requireStock.put((long) 1, (long) 10);
        Assertions.assertThrows(BaseException.class, () -> orderService.updateCoreProductsStock(requireStock));
    }



}
