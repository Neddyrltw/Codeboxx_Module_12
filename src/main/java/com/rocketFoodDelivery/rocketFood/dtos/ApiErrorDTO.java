package com.rocketFoodDelivery.rocketFood.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
/* Used to return API errors. */
public class ApiErrorDTO {
    String error;
    String details;
}
