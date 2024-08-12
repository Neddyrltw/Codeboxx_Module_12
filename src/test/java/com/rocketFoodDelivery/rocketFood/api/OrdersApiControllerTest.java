package com.rocketFoodDelivery.rocketFood.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.controller.api.OrderApiController;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.service.OrderService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)

public class OrdersApiControllerTest {  

    @InjectMocks
    private OrderApiController orderController;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void updateOrderStatus_Success() throws Exception {

        // Mock data
        int orderId = 2;
        String updatedStatus = "delivered";

        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus(updatedStatus);

        //Mock service behavior
        when(orderService.updateOrderStatus(orderId, updatedStatus)).thenReturn(statusDTO);

        // JSON request body
        String requestBody = objectMapper.writeValueAsString(statusDTO);

        // Perform POST request and assert value
        mockMvc.perform(post("/api/order/{order_id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(updatedStatus));
    }

    @Test
    void testUpdateRestaurant_NotFound() throws Exception {

        // Mock data
        int orderId = 1;
        String updatedStatus = "delivered";

        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus(updatedStatus);

        // Mock service behavior
        when(orderService.updateOrderStatus(orderId, updatedStatus)).thenReturn(statusDTO);

            // JSON request body
            String requestBody = objectMapper.writeValueAsString(statusDTO);

            // Perform POST request and assert response
            mockMvc.perform(post("/api/order/{order_id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(updatedStatus));
    }
}
