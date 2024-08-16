package com.rocketFoodDelivery.rocketFood.api;

//JUNIT TEST IMPORT
//importing the test annotation to run tests
import org.junit.jupiter.api.Test;

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
import com.rocketFoodDelivery.rocketFood.dtos.AuthRequestDTO;
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;

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
