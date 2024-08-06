package com.rocketFoodDelivery.rocketFood.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class AuthRequestDTO {

    @NotNull
    @Email
    private String email;

    @NotNull
    private String password;

    public AuthRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
