package com.concurrency.jpa.customer.order.service;


import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ActualProductRepository actualProductRepository;
    @Autowired
    private final CoreProductRepository coreProductRepository;


    @Override
    @Transactional
    public OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto){
        // 재고 확인하고 감소시키기
        updateCoreProductsStock(createOrderRequestDto.getCoreProducts());
        // 유형제품 찾기
        List<ActualProduct> actualProducts = concatActualProductList(createOrderRequestDto.getCoreProducts());
        // 주문 생성
        // 주문과 유형제품 연결 & 유형제품 상태 업데이트
        Order savedOrder = getOrder(createOrderRequestDto, actualProducts);

        return savedOrder.toDto();
    }

    @Override
    @Transactional
    public Order getOrder(CreateOrderRequestDto createOrderRequestDto, List<ActualProduct> actualProducts) {
        Order order = createOrderRequestDto.toEntity();
        order.addActualProducts(actualProducts);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActualProduct> concatActualProductList(Map<Long, Long> coreProducts) {
        List<ActualProduct> actualProducts = new ArrayList<>();
        coreProducts.forEach((coreProductId, stock) ->
                        actualProducts.addAll(
                                findActualProducts(
                                        coreProductId,
                                        ActualStatus.PENDING_ORDER,
                                        stock))
        );
        return actualProducts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActualProduct> findActualProducts(Long coreProductId, ActualStatus actualStatus, Long stock){
        return actualProductRepository.findByCoreProductIdAndActualStatus(
                coreProductId,
                actualStatus,
                PageRequest.of(0, Math.toIntExact(stock)));
    }

    /**
     * 요청한 상품의 유형제고가 충분한지 확인
     * @param requireProducts
     */
    @Override
    @Transactional
    public void updateCoreProductsStock(Map<Long, Long> requireProducts) {
        requireProducts.forEach(this::subtractCoreProductStock);
    }

    @Override
    @Transactional
    public long subtractCoreProductStock(Long coreProductId, Long reqStock){
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        if(reqStock > coreProduct.getStock()){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }
}
