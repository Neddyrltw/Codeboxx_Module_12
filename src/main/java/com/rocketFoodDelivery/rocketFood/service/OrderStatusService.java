package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.repository.OrderStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class OrderStatusService {
    private final OrderStatusRepository orderStatusRepository;

    public OrderStatusService(OrderStatusRepository orderStatusRepository) {
        this.orderStatusRepository = orderStatusRepository;
    }
    public List<OrderStatus> getAllOrderStatuses() {
        return orderStatusRepository.findAll();
    }
    public Optional<OrderStatus> findById(int id) {
        return orderStatusRepository.findById(id);
    }
    public Optional<OrderStatus> findByName(String name) {
        return orderStatusRepository.findByName(name);
    }
}