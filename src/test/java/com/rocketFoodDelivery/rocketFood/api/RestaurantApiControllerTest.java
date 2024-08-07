package com.rocketFoodDelivery.rocketFood.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
// STATIC IMPORTS
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// JAVA UTIL
import java.util.Optional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// JUNIT TEST IMPORT
import org.junit.jupiter.api.Test;

// MOCKITO IMPORTS
import org.mockito.InjectMocks;
import org.mockito.Mock;

// SPRING FRAMEWORK IMPORTS
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// JACKSON DATABIND IMPORT
import com.fasterxml.jackson.databind.ObjectMapper;

// PROJECT IMPORTS
import com.rocketFoodDelivery.rocketFood.controller.api.RestaurantApiController;
import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiProductForOrderApiDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.AuthRequestDTO;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
import com.rocketFoodDelivery.rocketFood.security.JwtUtil;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantApiControllerTest {

    @InjectMocks
    private RestaurantApiController restaurantController;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testFindRestaurantsByRatingAndPriceRange() throws Exception {

        // Mock data
        List<ApiRestaurantDto> mockData = new ArrayList<>();

        // Mock service behavior
        when(restaurantService.findRestaurantsByRatingAndPriceRange(eq(4), eq(3)))
            .thenReturn(mockData);

        // Perform GET request and validate response
        mockMvc.perform(MockMvcRequestBuilders.get("/api/restaurants")
        .param("rating", "4")
        .param("price_range", "3")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value(2))
        .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].name").value("Weimann, Brakus and Upton"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].price_range").value(3))
        .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].rating").value(4));
    }

    @Test
    public void testCreateRestaurant_Success() throws Exception {
        // Mock data
        ApiAddressDto inputAddress = new ApiAddressDto(1, "123 Wellington St.", "Montreal", "H1H2H2");
        ApiCreateRestaurantDto inputRestaurant = new ApiCreateRestaurantDto(1, 4, "Villa wellington", 2, "5144154415", "reservations@villawellington.com", inputAddress);

        // Mock service behavior
        when(restaurantService.createRestaurant(any())).thenReturn(Optional.of(inputRestaurant));

        // Validate response code and content
        mockMvc.perform(MockMvcRequestBuilders.post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(inputRestaurant)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(inputRestaurant.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.phone").value(inputRestaurant.getPhone()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(inputRestaurant.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.city").value(inputRestaurant.getAddress().getCity()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.street_address").value(inputRestaurant.getAddress().getStreetAddress()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.postal_code").value(inputRestaurant.getAddress().getPostalCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.user_id").value(inputRestaurant.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.price_range").value(inputRestaurant.getPriceRange()));
    }

    @Test
    public void testUpdateRestaurant_Success() throws Exception {
        // Mock data
        int restaurantId = 1;
        ApiCreateRestaurantDto updatedData = new ApiCreateRestaurantDto();
        updatedData.setName("Updated Name");
        updatedData.setPriceRange(2);
        updatedData.setPhone("555-1234");

        // Mock service behavior
        when(restaurantService.updateRestaurant(restaurantId, updatedData))
                .thenReturn(Optional.of(updatedData));

        // Validate response code and content
        mockMvc.perform(MockMvcRequestBuilders.put("/api/restaurants/{id}", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updatedData)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Updated Name"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.price_range").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.phone").value("555-1234"));
    }


}