package com.rocketFoodDelivery.rocketFood.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.rocketFoodDelivery.rocketFood.controller.api.OrderApiController;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiProductForOrderApiDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
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

// CREATE
@Test
@Transactional
public void createOrder_Success() throws Exception {
    // Prepare request data
    int restaurantId = 4;
    int customerId = 5;

    ApiOrderDTO requestOrder = ApiOrderDTO.builder()
        .restaurant_id(restaurantId)
        .customer_id(customerId)
        .products(Arrays.asList(
            ApiProductForOrderApiDTO.builder().id(24).quantity(3).build(),
            ApiProductForOrderApiDTO.builder().id(19).quantity(1).build(),
            ApiProductForOrderApiDTO.builder().id(20).quantity(1).build()
        ))
        .build();

    String requestBody = objectMapper.writeValueAsString(requestOrder);

    // Perform POST request and capture the result
    MvcResult mvcResult = mockMvc.perform(post("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isOk())
        .andReturn();

    // Extract the created orderId from the response
    String responseContent = mvcResult.getResponse().getContentAsString();
    int createdOrderId = JsonPath.read(responseContent, "$.id");

    // Perform GET request to retrieve the order by ID
    mockMvc.perform(get("/api/orders/" + createdOrderId)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(createdOrderId))
        .andExpect(jsonPath("$.customer_id").value(customerId))
        .andExpect(jsonPath("$.restaurant_id").value(restaurantId))
        .andExpect(jsonPath("$.status").value("in progress"))
        .andExpect(jsonPath("$.products[0].id").value(24))
        .andExpect(jsonPath("$.products[0].quantity").value(3))
        .andExpect(jsonPath("$.total_cost").value(1500));
}


    // RETRIEVE
    @Test
    public void getOrders_Success() throws Exception {

        // Mock data
        ApiOrderDTO order1 = ApiOrderDTO.builder()
            .id(3)
            .customer_id(5)
            .customer_name("Dr. Brett Bartell")
            .customer_address("0581 Wisozk Corner, Courtneyberg, 32318")
            .restaurant_id(4)
            .restaurant_name("Kulas, Funk and Moen")
            .restaurant_address("1963 Schinner Crossing, Hectorville, 84948-9652")
            .status("delivered")
            .products(Arrays.asList(
                ApiProductForOrderApiDTO.builder().id(24).product_name("Tuna Sashimi").quantity(3).unit_cost(300).total_cost(900).build(),
                ApiProductForOrderApiDTO.builder().id(19).product_name("Bunny Chow").quantity(1).unit_cost(300).total_cost(300).build(),
                ApiProductForOrderApiDTO.builder().id(20).product_name("Chicken Fajitas").quantity(1).unit_cost(300).total_cost(300).build(),
                ApiProductForOrderApiDTO.builder().id(22).product_name("Kebab").quantity(2).unit_cost(300).total_cost(300).build(),
                ApiProductForOrderApiDTO.builder().id(23).product_name("Pierogi").quantity(2).unit_cost(300).total_cost(300).build()
                ))
                .total_cost(2100)
                .build();

        ApiOrderDTO order2 = ApiOrderDTO.builder()
            .id(5)
            .customer_id(5)
            .customer_name("Dr. Brett Bartell")
            .customer_address("0581 Wisozk Corner, Courtneyberg, 32318")
            .restaurant_id(8)
            .restaurant_name("Hermann, Quigley and Donnelly")
            .restaurant_address("940 Labadie Square, Nolanville, 78540")
            .status("delivered")
            .products(Arrays.asList(
                ApiProductForOrderApiDTO.builder().id(30).product_name("Salmon Nigiri").quantity(3).unit_cost(500).total_cost(1500).build(),
                ApiProductForOrderApiDTO.builder().id(29).product_name("Som Tam").quantity(1).unit_cost(500).total_cost(500).build(),
                ApiProductForOrderApiDTO.builder().id(32).product_name("Chicken Milanese").quantity(1).unit_cost(500).total_cost(500).build()
            ))
            .total_cost(2500)
            .build();


        
        // Mock service behavior
        when(orderService.getOrdersByTypeAndId("customer", 5)).thenReturn(Arrays.asList(order1, order2));

        // Perform GET request and assert response
        mockMvc.perform(get("/api/orders")
        .param("type", "customer")
        .param("id", "5")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(order1.getId()))
        .andExpect(jsonPath("$[0].restaurant_id").value(order1.getRestaurant_id()))
        .andExpect(jsonPath("$[1].id").value(order2.getId()))
        .andExpect(jsonPath("$[1].restaurant_id").value(order2.getRestaurant_id()));
    }

    // UPDATE
    @Test
    public void updateOrderStatus_Success() throws Exception {

        // Mock data
        int orderId = 2;
        String updatedStatus = "in progress";

        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus(updatedStatus);

        //Mock service behavior
        when(orderService.updateOrderStatus(orderId, updatedStatus)).thenReturn(statusDTO);

        // JSON request body
        String requestBody = objectMapper.writeValueAsString(statusDTO);

        // Perform POST request and assert value
        mockMvc.perform(post("/api/orders/{order_id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(updatedStatus));
    }
    
    @Test
    public void updateOrderStatus_NotFound() throws Exception {
        // Mock data
        int orderId = 62;
        String updatedStatus = "in progress";

        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus(updatedStatus);

        // Mock service behavior to throw ResourceNotFoundException
        when(orderService.updateOrderStatus(orderId, updatedStatus))
            .thenThrow(new ResourceNotFoundException("Order not found"));

        // JSON request body
        String requestBody = objectMapper.writeValueAsString(statusDTO);

        // Perform POST request and assert not found response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/{order_id}/status", orderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Resource not found"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details").value("Order with id " + orderId + " not found"));
    }

    @Test
    public void updateOrderStatus_BadRequest() throws Exception {

        //Mockdata
        int orderId = 2;

        // Create an ApiOrderStatusDTO without setting status
        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus("");

        // JSON request body
        String requestBody = objectMapper.writeValueAsString(statusDTO);

        // Perform POST request and assert bad request response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/orders/{order_id}/status", orderId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(MockMvcResultMatchers.status().isBadRequest())
        .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid or missing parameters"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.details").isEmpty());
    }

    // DELETE

    
}
