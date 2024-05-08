package com.concurrency.jpa.customer.Product;

import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{
    private final ActualProductRepository actualProductRepository;
    private final CoreProductRepository coreProductRepository;

    /**
     * 요청한 상품의 유형제고가 충분한지 확인
     * @param requireProducts
     */
    @Override
    @Transactional
    public void updateCoreProductsStock(Map<Long, Long> requireProducts) {
        requireProducts.forEach(this::subtractCoreProductStockPessimistic);
    }

    @Override
    @Transactional
    public List<ActualProduct> concatActualProductList(Map<Long, Long> coreProducts) {
        List<ActualProduct> actualProducts = new ArrayList<>();
        coreProducts.forEach((coreProductId, stock) ->{
                    actualProducts.addAll(
                            findActualProducts(
                                    coreProductId,
                                    ActualStatus.PENDING_ORDER,
                                    stock));
                }
        );
        return actualProducts;
    }

    @Override
    @Transactional
    public List<ActualProduct> findActualProducts(Long coreProductId, ActualStatus actualStatus, Long stock){
        List<ActualProduct> actualProducts = actualProductRepository.findByCoreProduct_IdAndActualStatus(
                coreProductId,
                actualStatus,
                PageRequest.of(0, Math.toIntExact(stock)));
        return actualProducts;
    }

    @Override
    @Transactional
    public List<ActualProduct> findActualProductsByOrder(Long orderId){
        return actualProductRepository.findByOrder_IdPessimistic(orderId);
    }



    ///////////////////////// 재고량 감소

    @Override
    @Transactional
    public long subtractCoreProductStock(Long coreProductId, Long reqStock){
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        if (reqStock > coreProduct.getStock()) {
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long subtractCoreProductStockPessimistic(Long coreProductId, Long reqStock){
        CoreProduct coreProduct = coreProductRepository.findByIdPessimistic(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        long stock = coreProduct.getStock();
        if(reqStock > stock){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStockOptimistic(Long coreProductId, Long reqStock) {
        int patience = 0;
        while(true){
            try{
                try{
                    CoreProduct coreProduct = coreProductRepository.findById(coreProductId)
                            .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
                    if(reqStock > coreProduct.getStock()){
                        throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
                    }
                    return coreProduct.addStrock(-reqStock);
                }
                catch(Exception oe){
                    if(patience == 10){
                        throw new BaseException(BaseResponseStatus.OPTIMISTIC_FAILURE);
                    }
                    patience++;
                    Thread.sleep(500);
                }
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }

        }
    }
}
