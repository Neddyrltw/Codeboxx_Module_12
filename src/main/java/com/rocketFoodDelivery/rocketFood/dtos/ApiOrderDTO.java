package com.rocketFoodDelivery.rocketFood.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
//this is a class created to process the order details for the order api from postman.
public class ApiOrderDTO {
    private int id ;
    private int customer_id;
    private String customer_name;
    private String customer_address;
    private int restaurant_id;
    private String restaurant_name;
    private String restaurant_address;
    private String status;
    private List <ApiProductForOrderApiDTO> products;
    private long total_cost;
    private Integer restaurant_rating;

    public Integer getRestaurantRating() {
        return restaurant_rating;
    }

    public void setRestaurantRating(int restaurantRating) {
        this.restaurant_rating = restaurantRating;
    }
}
