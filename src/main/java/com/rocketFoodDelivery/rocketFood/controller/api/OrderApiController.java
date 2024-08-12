package com.rocketFoodDelivery.rocketFood.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.service.OrderService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/order")
public class OrderApiController {

    @Autowired
    OrderService orderService;

    @PostMapping("/{order_id}/status")
    public ResponseEntity<ApiOrderStatusDTO> updateOrderStatus(
        @PathVariable("order_id") int orderId,
        @Validated @RequestBody ApiOrderStatusDTO statusDTO) {
        
        // Update order status
        ApiOrderStatusDTO updatedStatusDTO = orderService.updateOrderStatus(orderId, statusDTO.getStatus());
        
        return new ResponseEntity<>(updatedStatusDTO, HttpStatus.OK);
    } 
}
