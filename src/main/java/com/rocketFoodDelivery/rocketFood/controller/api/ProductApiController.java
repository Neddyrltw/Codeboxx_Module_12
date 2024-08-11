package com.rocketFoodDelivery.rocketFood.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rocketFoodDelivery.rocketFood.dtos.ApiProductDTO;
import com.rocketFoodDelivery.rocketFood.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    private final ProductService productService;

    // Constructor injection to initialize productService
    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ApiProductDTO>> getProductsByRestaurant(@RequestParam("restaurant") int restaurantId) {
        List<ApiProductDTO> products = productService.findProductsByRestaurant(restaurantId);
        return ResponseEntity.ok(products);
    }
}