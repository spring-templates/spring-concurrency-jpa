package com.concurrency.jpa.customer.order.service;


import com.concurrency.jpa.customer.Product.ActualProductRepository;
import com.concurrency.jpa.customer.Product.CoreProductRepository;
import com.concurrency.jpa.customer.Product.entity.ActualProduct;
import com.concurrency.jpa.customer.Product.entity.CoreProduct;
import com.concurrency.jpa.customer.Product.enums.ActualStatus;
import com.concurrency.jpa.customer.common.BaseException;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.lock.LockService;
import com.concurrency.jpa.customer.order.Order;
import com.concurrency.jpa.customer.order.OrderRepository;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.payment.dto.PaymentInitialRequestDto;
import com.concurrency.jpa.customer.payment.dto.PaymentStatusDto;
import com.concurrency.jpa.customer.payment.service.PaymentService;
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
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ActualProductRepository actualProductRepository;
    private final CoreProductRepository coreProductRepository;
    private final PaymentService paymentService;
    private final LockService lockService;

    @Override
    @Transactional
    public PaymentStatusDto createOrder(CreateOrderRequestDto createOrderRequestDto){
        // 유저 권한 확인하기
        checkUserAuthority(createOrderRequestDto.getClientType());
        // 재고 확인하고 감소시키기
        updateCoreProductsStock(createOrderRequestDto.getCoreProducts());
        // 유형제품 찾기
        List<ActualProduct> actualProducts = concatActualProductList(createOrderRequestDto.getCoreProducts());
        // 주문 생성
        // 주문과 유형제품 연결 & 유형제품 상태 업데이트
        Order savedOrder = getOrder(createOrderRequestDto, actualProducts);
        PaymentStatusDto payPending= paymentService.pay(new PaymentInitialRequestDto(savedOrder.toDto()));
        savedOrder.setPaymentId(payPending.paymentId());
        return payPending;
    }

    private void checkUserAuthority(Actors clientType) {
        if(clientType.equals(Actors.Guest)){
            throw new BaseException(BaseResponseStatus.NOT_AUTHORITY);
        }
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

    ///////////////////////// 재고량 감소

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStock(Long coreProductId, Long reqStock){
        CoreProduct coreProduct = coreProductRepository.findById(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        if(reqStock > coreProduct.getStock()){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStockPessimistic(Long coreProductId, Long reqStock){
        CoreProduct coreProduct = coreProductRepository.findByIdPessimistic(coreProductId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL));
        if(reqStock > coreProduct.getStock()){
            throw new BaseException(BaseResponseStatus.NOT_ENOUGH_STOCK);
        }
        return coreProduct.addStrock(-reqStock);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public long subtractCoreProductStockOptimistic(Long coreProductId, Long reqStock) throws InterruptedException {
        int patience = 0;
        while(true){
            try{
                CoreProduct coreProduct = coreProductRepository.findByIdPessimistic(coreProductId)
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
                System.out.println("현재까지 "+patience+"번 참음");
                patience++;
                Thread.sleep(500);
            }
        }
    }

}
