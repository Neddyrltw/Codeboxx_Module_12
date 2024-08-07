package com.rocketFoodDelivery.rocketFood.api;

import com.rocketFoodDelivery.rocketFood.api.RestaurantApiControllerTest;

//STATIC IMPORTS
//import that allows flexible argument matches
import static org.mockito.ArgumentMatchers.any;
//importing mock functionalities
import static org.mockito.Mockito.*;

//JAVA UTIL
//importing the optional annotation, for potentially null values
import java.util.Optional;

import org.hamcrest.Matchers;
//JUNIT TEST IMPORT
//importing the test annotation to run tests
import org.junit.jupiter.api.Test;

//MOCKITO IMPORTS
//imporort that allows injecting mock dependencies into the test annotation
import org.mockito.InjectMocks;
//importing the mock annotation to create mock objects
import org.mockito.Mock;

//SPRING FRAMEWORK IMPORTS
//automatically injects dependencies into the annotation field
import org.springframework.beans.factory.annotation.Autowired;
//import that auto configures MockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//importing the spring boot test annotation for integration testing
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
//import to specify data types sent or expected JSON, plain text, etc.
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
//importing Spring MockMvc for testing MVC controllers
import org.springframework.test.web.servlet.MockMvc;
//importing spring to create requests for testing
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//importing spring class for verifying the results of MVC requests
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

//JACKSON DATABIND IMPORT
//importing annotation class used for working with JSON data
import com.fasterxml.jackson.databind.ObjectMapper;

//PROJECT IMPORTS
//importing the api controller for restaurants
import com.rocketFoodDelivery.rocketFood.controller.api.RestaurantApiController;
//importing the created restaurant data transfer object
//importing the repositories to access its data
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import com.rocketFoodDelivery.rocketFood.security.JwtUtil;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiProductForOrderApiDTO;
import com.rocketFoodDelivery.rocketFood.dtos.AuthRequestDTO;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
//importing the functionality of the restaurantservice file for testing
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourierRepository courierRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @Test
    public void testLoginAttempt_Success() throws Exception {
        AuthRequestDTO loginAttempt = new AuthRequestDTO();
        loginAttempt.setEmail("erica.ger@gmail.com");
        loginAttempt.setPassword("password");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(loginAttempt)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.success").value("true"));
    }
}
