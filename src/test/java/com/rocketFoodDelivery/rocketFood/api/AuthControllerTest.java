package com.rocketFoodDelivery.rocketFood.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.controller.api.AuthController;
import com.rocketFoodDelivery.rocketFood.dtos.AuthRequestDTO;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.security.JwtUtil;
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import org.junit.jupiter.api.Test;  
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CourierRepository courierRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @Test
    public void testAuthenticate_Success() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO("john.doe@codeboxx.com", "password");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setEmail("john.doe@codeboxx.com");
        userEntity.setPassword("password");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userEntity, null);

        when(authManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateAccessToken(any(UserEntity.class))).thenReturn("key");

        mockMvc.perform(post("/api/auth")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("key"));
    }

    @Test
    public void testAuthenticate_Unauthorized() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO("john.doe@codeboxx.com", "wrongpassword");

        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

        mockMvc.perform(post("/api/auth")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
