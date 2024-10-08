package com.rocketFoodDelivery.rocketFood.api;

// HAMCREST LIBRARY
import static org.hamcrest.Matchers.nullValue;

import static org.mockito.Mockito.*;

// JAVA UTIL
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

// JACKSON DATABIND IMPORT
import com.rocketFoodDelivery.rocketFood.controller.api.ProductApiController;

// PROJECT IMPORTS
import com.rocketFoodDelivery.rocketFood.dtos.ApiProductDTO;
import com.rocketFoodDelivery.rocketFood.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProductApiControllerTest {

    @InjectMocks
    private ProductApiController productController;

    @Mock
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    // CREATE

    // RETRIEVE
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

    // UPDATE

    // DELETE
    
}
