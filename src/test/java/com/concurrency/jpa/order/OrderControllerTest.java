package com.concurrency.jpa.order;

import com.concurrency.jpa.customer.common.BaseResponse;
import com.concurrency.jpa.customer.order.OrderController;
import com.concurrency.jpa.customer.order.dto.CreateOrderRequestDto;
import com.concurrency.jpa.customer.order.enums.Actors;
import com.concurrency.jpa.customer.order.enums.PaymentMethods;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnRequestData() throws Exception {
        Map<Long, Long> coreProduct = new HashMap<>();
        coreProduct.put((long) 1,(long) 10);
        coreProduct.put((long)2, (long)5);
        CreateOrderRequestDto createOrderRequestDto = new CreateOrderRequestDto(coreProduct, Actors.InexperiencedCustomer, PaymentMethods.CREDIT_CARD);
        String content = objectMapper.writeValueAsString(createOrderRequestDto);
        System.out.println(content);
        mockMvc.perform(post("/order")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(content));
    }

}
