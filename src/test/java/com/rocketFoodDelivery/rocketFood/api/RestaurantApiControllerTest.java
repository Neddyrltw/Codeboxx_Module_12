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

// STATIC SPRING FRAMEWORK IMPORTS
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

    @Autowired
    private ObjectMapper objectMapper;

    // CREATE
    @Test
    void testCreateRestaurant_Success() throws Exception {

        // Example request payload
        String requestBody = "{"
                + "\"user_id\": 2,"
                + "\"name\": \"Villa Wellington\","
                + "\"phone\": \"15141234567\","
                + "\"email\": \"villa@wellington.com\","
                + "\"price_range\": 2,"
                + "\"address\": {"
                + "\"street_address\": \"123 Wellington St.\","
                + "\"city\": \"Montreal\","
                + "\"postal_code\": \"H3G264\""
                + "}"
                + "}";

        // Perform POST request and assert response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Success"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value("Villa Wellington"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.phone").value("15141234567"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value("villa@wellington.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.user_id").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.price_range").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.street_address").value("123 Wellington St."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.city").value("Montreal"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.address.postal_code").value("H3G264"));
    }

    // READ
    @Test
    public void testGetRestaurantById_Success() throws Exception {

        // Mock data
        int restaurantId = 2;
        String expectedName = "Weimann, Brakus and Upton";
        int expectedPriceRange = 3;
        int expectedRating = 4;

        // Create a Restaurant DTO
        ApiRestaurantDto mockRestaurant = new ApiRestaurantDto(restaurantId, expectedName, expectedPriceRange, expectedRating);

        // Mock service behavior
        when(restaurantService.findRestaurantWithAverageRatingById(restaurantId)).thenReturn(Optional.of(mockRestaurant));

        // Perform GET request and assert response
        mockMvc.perform(get("/api/restaurants/{id}", restaurantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(restaurantId))
                .andExpect(jsonPath("$.data.name").value(expectedName))
                .andExpect(jsonPath("$.data.price_range").value(expectedPriceRange))
                .andExpect(jsonPath("$.data.rating").value(expectedRating));
    }

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

    // UPDATE
    // Update Success
    @Test
    void testUpdateRestaurant_Success() throws Exception {

        // Mock data
        int restaurantId = 1;
        String updatedName = "B12 Nation";
        int updatedPriceRange = 3;
        String updatedPhone = "2223334444";
    
        // Mocks service behavior
        ApiCreateRestaurantDto updateDto = new ApiCreateRestaurantDto();
        updateDto.setName(updatedName);
        updateDto.setPriceRange(updatedPriceRange);
        updateDto.setPhone(updatedPhone);
    
        // Mock PUT request and validate response
        mockMvc.perform(MockMvcRequestBuilders.put("/api/restaurants/{id}", restaurantId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(restaurantId))
            .andExpect(jsonPath("$.data.name").value(expectedName))
            .andExpect(jsonPath("$.data.price_range").value(expectedPriceRange))
            .andExpect(jsonPath("$.data.rating").value(expectedRating))
            .andExpect(jsonPath("$.message").value("Success"));
    }
    
    // Update 404 (Not Found) fail
    @Test
    void testUpdateRestaurant_NotFound() throws Exception {

        // Mock data
        int nonExistentId = 9;

        // Mock service behavior
        ApiCreateRestaurantDto updateDto = new ApiCreateRestaurantDto();
        updateDto.setName("Nonexistent Restaurant");
        updateDto.setPriceRange(3);
        updateDto.setPhone("0000000000");
    
        // Mock PUT request with id not stored in database
        mockMvc.perform(MockMvcRequestBuilders.put("/api/restaurants/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource not found"))
                .andExpect(jsonPath("$.details").value("Restaurant with id " + nonExistentId + " not found."));
    }
    // Update 400 (Bad Request) fail
    @Test
    void testUpdateRestaurant_ValidationFailed() throws Exception {

        // Mock data
        int restaurantId = 9;

        // Mock service behavior
        ApiCreateRestaurantDto updateDto = new ApiCreateRestaurantDto();
        updateDto.setName("Invalid Restaurant");
        updateDto.setPriceRange(5);  // Invalid value (should be between 1 and 3)
        updateDto.setPhone("1111111111");

        // Mock PUT request with invalid input value
        mockMvc.perform(MockMvcRequestBuilders.put("/api/restaurants/{id}", restaurantId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details").exists());
}
    // DELETE
    @Test
    void testDeleteRestaurant_Success() throws Exception {
        // Mock data
        int restaurantId = 19;
    
        // Mock service behavior
        ApiRestaurantDto deleteDto = new ApiRestaurantDto();
        deleteDto.setId(restaurantId); // Ensure ID is set
        deleteDto.setName("Villa Wellington");
        deleteDto.setPriceRange(2);
        deleteDto.setRating(0);
    
        // Assuming `restaurantService.deleteRestaurant` is mocked
        when(restaurantService.deleteRestaurant(restaurantId)).thenReturn(deleteDto);
    
        // Mock DELETE request and validate response
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/restaurants/{id}", restaurantId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(restaurantId))
                .andExpect(jsonPath("$.data.name").value("Villa Wellington"))
                .andExpect(jsonPath("$.data.price_range").value(2)) 
                .andExpect(jsonPath("$.data.rating").value(0));
    }
    
}
