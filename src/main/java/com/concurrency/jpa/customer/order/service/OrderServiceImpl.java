package com.concurrency.jpa.customer.order.service;


import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.dto.StockDto;
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
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ActualProductRepository actualProductRepository;
    @Autowired
    private final CoreProductRepository coreProductRepository;


    @Transactional
    public OrderDto createOrder(CreateOrderRequestDto createOrderRequestDto){
        // 재고 확인하고 감소시키기
        updateCoreProducts(createOrderRequestDto.getCoreProducts());
        // 유형제품 찾기
        List<ActualProduct> actualProducts = new ArrayList<>();
        createOrderRequestDto.getCoreProducts()
                .forEach((coreProductId, value) ->
                        actualProducts.addAll(actualProductRepository.findByCoreProductIdAndActualStatus(
                                coreProductId,
                                ActualStatus.PENDING_ORDER,
                                PageRequest.of(0, Math.toIntExact(value)))));
        // 주문 생성
        Order order = createOrderRequestDto.toEntity();
        Order savedOrder = orderRepository.save(order);
        // 주문과 유형제품 연결 & 유형제품 상태 업데이트
        savedOrder.addActualProducts(actualProducts);


        return savedOrder.toDto();
    }

    /**
     * 요청한 상품의 유형제고가 충분한지 확인
     * @param requireProducts
     */
    @Transactional
    public void updateCoreProducts(Map<Long, Long> requireProducts) {
        requireProducts.forEach((k,v) -> {
            CoreProduct coreProduct = coreProductRepository.findById(k)
                    .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
            if(v > coreProduct.getStock()){
                throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
            }
            coreProduct.updateStrock(-v);
        });
    }
}
