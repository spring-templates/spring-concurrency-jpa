package com.concurrency.jpa.customer.order.service;


import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
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
import org.springframework.data.domain.Pageable;
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



    @Transactional
    public OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto){
        validateCoreProducts(createOrderRequestDto.getCoreProducts());
        // 유형제품 찾기
        List<ActualProduct> actualProducts = new ArrayList<>();
        createOrderRequestDto.getCoreProducts().forEach((coreProductId, value) ->
                actualProducts.addAll(actualProductRepository.findByCoreProductIdAndActualStatus(coreProductId, ActualStatus.PENDING_ORDER,
                        PageRequest.of(0, Math.toIntExact(value)))));
        System.out.println(actualProducts.size());
        // 주문 생성
        Order order = createOrderRequestDto.of();
        order.addActualProducts(actualProducts);

        // 유형제품 상태 업데이트
        updateProductsStatus(actualProducts, ActualStatus.PROCESSING);
        return order.toDto();
    }

    /**
     * 요청한 상품의 유형제고가 충분한지 확인
     * @param requireProducts
     */
    @Transactional
    public void validateCoreProducts(Map<Long, Long> requireProducts) {
        requireProducts.forEach((k,v) -> {
            if(v > actualProductRepository.countByCoreProductIdANDActualStatus(k, ActualStatus.PENDING_ORDER)){
                throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
            }
        });
    }

    @Transactional
    public void updateProductsStatus(List<ActualProduct> actualProducts, ActualStatus actualStatus) {
        actualProducts.forEach(a -> {
            a.updateActualProductStatus(actualStatus);
        });
    }

}
