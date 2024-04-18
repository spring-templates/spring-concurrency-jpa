package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{
    @Autowired
    private final ActualProductRepository actualProductRepository;
    /**
     * 요청한 상품의 유형제고가 충분한지 확인
     * @param requireProducts
     */
    public void validateCoreProducts(Map<Long, Long> requireProducts) {
        requireProducts.forEach((k,v) -> {
            if(v > actualProductRepository.countByCoreProductIdANDActualStatus(k, ActualStatus.PENDING_ORDER)){
                throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
            }
        });
    }
}
