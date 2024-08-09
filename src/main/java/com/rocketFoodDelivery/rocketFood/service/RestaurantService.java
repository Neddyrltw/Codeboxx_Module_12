package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiCreateRestaurantDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiRestaurantDto;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.models.UserEntity;
import com.rocketFoodDelivery.rocketFood.repository.*;

import jakarta.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;


@Service
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductOrderRepository productOrderRepository;
    private final UserRepository userRepository;
    private final AddressService addressService;
    private final AddressRepository addressRepository;
    @Autowired
    public RestaurantService(
        RestaurantRepository restaurantRepository,
        ProductRepository productRepository,
        OrderRepository orderRepository,
        ProductOrderRepository productOrderRepository,
        UserRepository userRepository,
        AddressService addressService,
        AddressRepository addressRepository
        ) {
        this.restaurantRepository = restaurantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.productOrderRepository = productOrderRepository;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.addressRepository = addressRepository;
    }

    /**
     * Creates a new restaurant and returns its information.
     *
     * @param restaurant The data for the new restaurant.
     * @return An Optional containing the created restaurant's information as an ApiCreateRestaurantDto,
     *         or Optional.empty() if the user with the provided user ID does not exist or if an error occurs during creation.
     */
    @Modifying
    @Transactional
    public Optional<ApiCreateRestaurantDto> createRestaurant(ApiCreateRestaurantDto inputDto) {
        // Check if user exists
        UserEntity userEntity = userRepository.findById(inputDto.getUserId()).orElse(null);
    
        if (userEntity == null) {
            // Return an empty Optional if no user exists with the given ID
            return Optional.empty();
        }
    
        // Check if address exists using AddressService
        Address address = addressService.findById(inputDto.getAddress().getId())
            .orElseGet(() -> {
                // Create a new address if it doesn't exist
                Address newAddress = Address.builder()
                        .streetAddress(inputDto.getAddress().getStreetAddress())
                        .city(inputDto.getAddress().getCity())
                        .postalCode(inputDto.getAddress().getPostalCode())
                        .build();
                return addressService.saveAddress(newAddress); // Save using AddressService
            });
    
        // Use the native SQL query to save the restaurant
        restaurantRepository.saveRestaurant(
            inputDto.getUserId(),
            address.getId(),
            inputDto.getName(),
            inputDto.getPriceRange(),
            inputDto.getPhone(),
            inputDto.getEmail()
        );
        
        // Retrieve the last inserted restaurant ID using a custom query
        int lastInsertedId = restaurantRepository.findLastInsertedId()
                .orElseThrow(() -> new RuntimeException("Failed to retrieve the last inserted restaurant ID"));

        // Retrieve the saved restaurant using its ID
        Restaurant savedRestaurant = restaurantRepository.findById(lastInsertedId)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve the saved restaurant"));

        // Convert the saved restaurant to ApiCreateRestaurantDto
        ApiCreateRestaurantDto responseDto = new ApiCreateRestaurantDto(
            savedRestaurant.getId(),
            savedRestaurant.getUserEntity().getId(),
            savedRestaurant.getName(),
            savedRestaurant.getPriceRange(),
            savedRestaurant.getPhone(),
            savedRestaurant.getEmail(),
            new ApiAddressDto(
                savedRestaurant.getAddress().getId(),
                savedRestaurant.getAddress().getStreetAddress(),
                savedRestaurant.getAddress().getCity(),
                savedRestaurant.getAddress().getPostalCode()
            )
        );
    
        // Return the created restaurant as ApiCreateRestaurantDto
        return Optional.of(responseDto);
    }

    // Retrieves all restaurants in the database
    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Retrieves a restaurant with its details, including the average rating, based on the provided restaurant ID.
     *
     * @param id The unique identifier of the restaurant to retrieve.
     * @return An Optional containing a RestaurantDTO with details such as id, name, price range, and average rating.
     *         If the restaurant with the given id is not found, an empty Optional is returned.
     *
     * @see RestaurantRepository#findRestaurantWithAverageRatingById(int) for the raw query details from the repository.
     */
    public Optional<ApiRestaurantDto> findRestaurantWithAverageRatingById(int id) {
        List<Object[]> restaurant = restaurantRepository.findRestaurantWithAverageRatingById(id);
    
        if (!restaurant.isEmpty()) {
            Object[] row = restaurant.get(0);
            int restaurantId = (int) row[0];
            String name = (String) row[1];
            int priceRange = (int) row[2];
            BigDecimal ratingBigDecimal = (BigDecimal) row[3];
            double rating = (ratingBigDecimal != null) ? ratingBigDecimal.setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
            int roundedRating = (int) Math.ceil(rating);
            ApiRestaurantDto restaurantDto = new ApiRestaurantDto(restaurantId, name, priceRange, roundedRating);
            return Optional.of(restaurantDto);
        } else {
            return Optional.empty();
        }
    }
    
    /**
     * Finds restaurants based on the provided rating and price range.
     *
     * @param rating     The rating for filtering the restaurants.
     * @param priceRange The price range for filtering the restaurants.
     * @return A list of ApiRestaurantDto objects representing the selected restaurants.
     *         Each object contains the restaurant's ID, name, price range, and a rounded-up average rating.
     */
    public List<ApiRestaurantDto> findRestaurantsByRatingAndPriceRange(Integer rating, Integer priceRange) {
        System.out.println("Rating parameter: " + rating);
        System.out.println("Price range parameter: " + priceRange);

        // Fetch  list of restaurants from repository
        List<Object[]> restaurants = restaurantRepository.findRestaurantsByRatingAndPriceRange(rating, priceRange);

        // Create empty list to hold  the API Restaurant DTOs
        List<ApiRestaurantDto> restaurantDtos = new ArrayList<>();

            // Loop through each row of fetched list
            for (Object[] row : restaurants) {
                //Extract restaurant id from first element of row
                int restaurantId = (int) row[0];
                // Extract name of restaurant from second element
                String name = (String) row[1];
                // Extract price range from third element
                int range = (int) row[2];
                // Extract avg. rating for fourth element(if !null)
                double avgRating = (row[3] != null) ? ((BigDecimal) row[3]).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
                // Round avg. rating up 
                int roundedAvgRating = (int) Math.ceil(avgRating);
                // Create new RestaurantDTO obj from extracted values
                restaurantDtos.add(new ApiRestaurantDto(restaurantId, name, range, roundedAvgRating));
            }
            // Return the list of objects
            return restaurantDtos;
    }

    /**
     * Finds a restaurant by its ID.
     *
     * @param id The ID of the restaurant to retrieve.
     * @return An Optional containing the restaurant with the specified ID,
     *         or Optional.empty() if no restaurant is found.
     */
    public Optional<Restaurant> findById(int id) {
        return null; // TODO return proper object
    }

    /**
     * Updates an existing restaurant by ID with the provided data.
     *
     * @param id                  The ID of the restaurant to update.
     * @param updatedRestaurantDto The updated data for the restaurant.
     * @return An Optional containing the updated restaurant's information as an ApiCreateRestaurantDto,
     *         or Optional.empty() if the restaurant with the specified ID is not found or if an error occurs during the update.
     */
    @Transactional
    public ApiCreateRestaurantDto updateRestaurant(int restaurantId, ApiCreateRestaurantDto restaurantUpdateData) {
    
        // Check if restaurant exists
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant with id " + restaurantId + " not found.");
        }
        
        // Validate input values
        if (restaurantUpdateData.getPriceRange() < 1 || restaurantUpdateData.getPriceRange() > 3) {
            throw new ValidationException("Invalid price range: " + restaurantUpdateData.getPriceRange());
        }
    
        // Perform update
        restaurantRepository.updateRestaurant(
            restaurantId,
            restaurantUpdateData.getName(),
            restaurantUpdateData.getPriceRange(),
            restaurantUpdateData.getPhone()
        );
    
        // Return updated restaurant details
        return restaurantRepository.findById(restaurantId)
            .map(updatedRestaurant -> {
                ApiCreateRestaurantDto dto = new ApiCreateRestaurantDto();
                dto.setId(updatedRestaurant.getId());
                dto.setUserId(updatedRestaurant.getUserEntity().getId());
                dto.setName(updatedRestaurant.getName());
                dto.setPriceRange(updatedRestaurant.getPriceRange());
                dto.setPhone(updatedRestaurant.getPhone());
                dto.setEmail(updatedRestaurant.getEmail());
                dto.setAddress(new ApiAddressDto(
                    updatedRestaurant.getAddress().getId(),
                    updatedRestaurant.getAddress().getStreetAddress(),
                    updatedRestaurant.getAddress().getCity(),
                    updatedRestaurant.getAddress().getPostalCode()
                ));
                return dto;
            })
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant with id " + restaurantId + " not found."));
    }    

    /**
     * Deletes a restaurant along with its associated data, including its product orders, orders and products.
     *
     * @param restaurantId The ID of the restaurant to delete.
     */
    @Transactional
    public ApiRestaurantDto deleteRestaurant(int restaurantId) {
        // Fetch restaurant details and rating
        List<Object[]> results = restaurantRepository.findRestaurantWithAverageRatingById(restaurantId);

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Restaurant with id: " + restaurantId + " not found");
        }

        // Retrieve restaurant details
        Object[] result = results.get(0);
        int id = (Integer) result[0];
        String name = (String) result[1];
        int priceRange = (Integer) result[2];
        System.out.println(priceRange);

        // Handle rating conversion from BigDecimal to int
        BigDecimal ratingBigDecimal = (BigDecimal) result[3];
        double ratingDouble = (ratingBigDecimal != null) ? ratingBigDecimal.setScale(1, RoundingMode.HALF_UP).doubleValue() : 0.0;
        int roundedRating = (int) Math.ceil(ratingDouble);

        // Delete the restaurant
        restaurantRepository.deleteRestaurantById(restaurantId);

        // Prepare the response data
        ApiRestaurantDto responseDto = new ApiRestaurantDto();
        responseDto.setId(id);
        responseDto.setName(name);
        responseDto.setPriceRange(priceRange);
        responseDto.setRating(roundedRating);

        return responseDto;
    }

}