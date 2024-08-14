package com.rocketFoodDelivery.rocketFood.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.service.OrderService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Autowired
    OrderService orderService;

    /**
     * Retrieves a list of orders based on user type and ID.
     *
     * This endpoint returns a list of orders filtered by user type (customer, restaurant, or courier) 
     * and the corresponding ID.
     * 
     * @param type The type of the user (customer, restaurant, courier).
     * @param id   The ID of the user.
     * @return A ResponseEntity containing a list of orders.
     */
    @GetMapping
    public ResponseEntity<List<ApiOrderDTO>> getOrders(
        @RequestParam String type,
        @RequestParam int id) {

        List<ApiOrderDTO> orders = orderService.getOrdersByTypeAndId(type, id);
        
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    /**
     * Updates the status of an order.
     *
     * This endpoint updates the status of an existing order by its ID.
     * 
     * @param orderId   The ID of the order to update.
     * @param statusDTO The status to set for the order.
     * @return A ResponseEntity containing the updated order status.
     */
    @PostMapping("/{order_id}/status")
    public ResponseEntity<ApiOrderStatusDTO> updateOrderStatus(
        @PathVariable("order_id") int orderId,
        @Validated @RequestBody ApiOrderStatusDTO statusDTO) {
        
        // Update order status
        ApiOrderStatusDTO updatedStatusDTO = orderService.updateOrderStatus(orderId, statusDTO.getStatus());
        
        return new ResponseEntity<>(updatedStatusDTO, HttpStatus.OK);
    } 
}
