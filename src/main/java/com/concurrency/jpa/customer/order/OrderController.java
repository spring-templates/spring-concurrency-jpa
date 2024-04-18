package com.concurrency.jpa.customer.order;

import com.concurrency.jpa.customer.common.BaseResponse;
import com.concurrency.jpa.customer.common.BaseResponseStatus;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

//    @PostMapping("/order")
//    public BaseResponse<CreateOrderRequestDto> postOrder(@RequestBody CreateOrderRequestDto createOrderRequestDto){
//        return new BaseResponse<>(createOrderRequestDto);
//    }
    @PostMapping("/order")
    public CreateOrderRequestDto postOrder(@RequestBody CreateOrderRequestDto createOrderRequestDto){
        return createOrderRequestDto;
    }
}
