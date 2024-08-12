package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.models.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public ApiOrderStatusDTO updateOrderStatus(int orderId, String newStatus) {
        // Find the order by id or throw error
        Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order with id " + orderId + " not found"));

        // Update order status
        order.getOrder_status().setName(newStatus);

        // Save order status
        orderRepository.save(order);

        // Create and return a DTO with updated status
        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus(newStatus);

        return statusDTO;
    }
}