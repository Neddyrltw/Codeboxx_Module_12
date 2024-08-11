package com.rocketFoodDelivery.rocketFood.api;

// HAMCREST LIBRARY
import static org.hamcrest.Matchers.nullValue;

// JUNIT LIBRARY
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
import java.util.Arrays;

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
import com.rocketFoodDelivery.rocketFood.controller.api.ProductApiController;

// PROJECT IMPORTS
import com.rocketFoodDelivery.rocketFood.controller.api.RestaurantApiController;
import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiProductDTO;
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
import com.rocketFoodDelivery.rocketFood.service.ProductService;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProductApiControllerTest {

    @InjectMocks
    private ProductApiController productController;

    @Mock
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetProductsByRestaurant_Success() throws Exception {
        int restaurantId = 5;
        ApiProductDTO product1 = new ApiProductDTO(25, "Meatballs with Sauce", 400, "Description for Product 4", restaurantId);
        ApiProductDTO product2 = new ApiProductDTO(26, "Ebiten maki", 400, "Description for Product 4", restaurantId);

        List<ApiProductDTO> products = Arrays.asList(product1, product2);

        when(productService.findProductsByRestaurant(restaurantId)).thenReturn(products);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                .param("restaurant", String.valueOf(restaurantId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(25))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Meatballs with Sauce"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cost").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Description for Product 4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].restaurantId").value(restaurantId)) 
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(26))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Ebiten maki"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].cost").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].description").value("Description for Product 4"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].restaurantId").value(restaurantId));
    }


    @Test
    void testGetProductsByRestaurant_InvalidParameters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Invalid or missing parameters"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details").value(nullValue()));
    }


    
}
