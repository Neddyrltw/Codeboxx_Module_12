package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiProductDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;

    // Constructor injection
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves a list of products for a given restaurant.
     *
     * @param restaurantId The ID of the restaurant to retrieve products for.
     * @return A list of ApiProductDTO objects representing the products.
     * @throws ResourceNotFoundException if no products are found for the given restaurant ID.
     */
    public List<ApiProductDTO> findProductsByRestaurant(int restaurantId) {
        // Fetch products using the repository method
        List<Object[]> results = productRepository.findProductsByRestaurantId(restaurantId);
        List<ApiProductDTO> products = new ArrayList<>();

        // Convert the results to ApiProductDTO
        for (Object[] row : results) {
            int id = (int) row[0];
            String name = (String) row[1];
            int cost = (int) row[2];
            String description = (String) row[3];
            int restId = (int) row[4];

            products.add(new ApiProductDTO(id, name, cost, description, restId));
        }

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product with id " + restaurantId + " not found");
        }

        return products;
    }
}
