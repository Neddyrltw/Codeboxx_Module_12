package com.rocketFoodDelivery.rocketFood.dtos;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiProductDTO {
    private int id;
    private String name;
    private int cost;
    private String description;
    private int restaurantId;
}
