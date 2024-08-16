package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiProductForOrderApiDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
import com.rocketFoodDelivery.rocketFood.models.Customer;
import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.models.ProductOrder;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusService orderStatusService;
    private final CustomerService customerService;
    private final RestaurantService restaurantService;
    private final ProductRepository productRepository;
    private final AddressService addressService;
    private final RestaurantRepository restaurantRepository;

    public OrderService(OrderRepository orderRepository, OrderStatusService orderStatusService, 
                        CustomerService customerService, RestaurantService restaurantService,
                        ProductRepository productRepository, AddressService addressService,
                        RestaurantRepository restaurantRepository) {  
        this.orderRepository = orderRepository;
        this.orderStatusService = orderStatusService;
        this.customerService = customerService;
        this.restaurantService = restaurantService;
        this.productRepository = productRepository;
        this.addressService = addressService;
        this.restaurantRepository = restaurantRepository;
    }

    // CREATE

/**
 * Creates a new order based on the provided `ApiOrderDTO`.
 *
 * This method performs the following steps:
 * 1. Retrieves the customer by their ID from the `ApiOrderDTO`. If the customer does not exist, throws a `ResourceNotFoundException`.
 * 2. Retrieves the restaurant by its ID from the `ApiOrderDTO`. If the restaurant does not exist, throws a `ResourceNotFoundException`.
 * 3. Fetches the `OrderStatus` for the new order, defaulting to "in progress". If the status does not exist, throws a `ResourceNotFoundException`.
 * 4. Creates a new `Order` entity, associating it with the retrieved customer, restaurant, and order status.
 * 5. Iterates through the product details provided in the `ApiOrderDTO`, fetching each `Product` by its ID. If a product is not found, throws a `ResourceNotFoundException`.
 * 6. Constructs `ProductOrder` entities for each product, linking them to the new `Order` and setting their quantity and unit cost.
 * 7. Associates the list of `ProductOrder` entities with the newly created `Order`.
 * 8. Saves the new `Order` entity to the database.
 * 9. Maps the saved `Order` to an `ApiOrderDTO` and returns it.
 *
 * @param apiOrderDTO The data transfer object containing the details of the order to be created.
 * @return An `ApiOrderDTO` containing the details of the newly created order.
 * @throws ResourceNotFoundException if the customer, restaurant, product, or order status specified in the DTO does not exist.
 */
@Transactional
public ApiOrderDTO createOrder(ApiOrderDTO apiOrderDTO) {
    // Fetch the customer by ID
    Customer customer = customerService.findById(apiOrderDTO.getCustomer_id())
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

    // Fetch the restaurant by ID
    Restaurant restaurant = restaurantService.findById(apiOrderDTO.getRestaurant_id())
            .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

    // Fetch the order status by name
    OrderStatus orderStatus = orderStatusService.findByName("in progress")
            .orElseThrow(() -> new ResourceNotFoundException("Order status not found"));

    // Retrieve the restaurant rating using existing logic
    Integer restaurantRating = getRestaurantRating(restaurant.getId());

    // Create a new Order entity
    Order newOrder = Order.builder()
        .customer(customer)
        .restaurant(restaurant)
        .order_status(orderStatus)
        .restaurant_rating(restaurantRating)
        .build();

    // Attach products
    List<ProductOrder> productOrders = apiOrderDTO.getProducts().stream()
            .map(dto -> {
                Product product = productRepository.findById(dto.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                return ProductOrder.builder()
                        .product(product)
                        .order(newOrder)
                        .product_quantity(dto.getQuantity())
                        .product_unit_cost(product.getCost())
                        .build();
            })
            .collect(Collectors.toList());

    newOrder.setProducts(productOrders);

    // Save the new order
    orderRepository.save(newOrder);

    // Create and return a DTO with the newly created order's information
    return mapToApiOrderDTO(newOrder);
}
    
    // RETRIEVE

    /**
     * Retrieves a list of orders based on the user type and ID.
     *
     * This method performs the following steps:
     * 1. Validates the input parameters. If the `type` is null or the `id` is not a positive number, it throws a BadRequestException.
     * 2. Based on the `type` parameter, retrieves orders for a customer, restaurant, or courier using the corresponding repository methods.
     * 3. If no orders are found for the given criteria, throws a ResourceNotFoundException.
     * 4. Maps each retrieved `Order` entity to an `ApiOrderDTO`, including nested product details, and returns the list of DTOs.
     *
     * @param type The type of user ("customer", "restaurant", or "courier").
     * @param id   The ID of the user (customer, restaurant, or courier).
     * @return A list of `ApiOrderDTO` objects representing the orders for the specified user.
     * @throws BadRequestException if the input parameters are invalid.
     * @throws ResourceNotFoundException if no orders are found for the given criteria.
     */
    public List<ApiOrderDTO> getOrdersByTypeAndId(String type, int id) {
        // Validate input
        if (type == null || id <= 0) {
            throw new BadRequestException("Invalid or missing parameters");
        }

        // Fetch orders based on type
        List<Order> orders;
        switch (type.toLowerCase()) {
            case "customer":
                orders = orderRepository.findOrdersByCustomerId(id);
                break;
            case "restaurant":
                orders = orderRepository.findOrdersByRestaurantId(id);
                break;
            case "courier":
                orders = orderRepository.findOrdersByCourierId(id);
                break;
            default:
                throw new BadRequestException("Invalid type parameter");
        }

        // Check if orders were found
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for the given criteria");
        }

        // 4. Assemble response
        return orders.stream()
            .map(this::mapToApiOrderDTO)
            .collect(Collectors.toList());
    }

    /**
     * Retrieves an order by its ID and maps it to an `ApiOrderDTO` object.
     *
     * This method performs the following steps:
     * 1. Fetches the `Order` entity from the database using the provided order ID.
     * 2. If the order is not found, throws a `ResourceNotFoundException`.
     * 3. Maps the `Order` entity to an `ApiOrderDTO` using the `mapToApiOrderDTO` method.
     * 4. Returns the `ApiOrderDTO` containing the details of the retrieved order.
     *
     * @param orderId The ID of the order to retrieve.
     * @return An `ApiOrderDTO` object representing the order with the specified ID.
     * @throws ResourceNotFoundException if the order with the given ID is not found.
     */
    public ApiOrderDTO getOrderById(int orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order with id " + orderId + " not found"));

        return mapToApiOrderDTO(order);
    }

    /**
    * Retrieves all orders and maps them to `ApiOrderDTO` objects.
    *
    * This method performs the following steps:
    * 1. Fetches all `Order` entities from the database.
    * 2. Maps each `Order` entity to an `ApiOrderDTO` using the `mapToApiOrderDTO` method.
    * 3. Returns a list of `ApiOrderDTO` objects containing the details of each order.
    *
    * @return A list of `ApiOrderDTO` objects representing all the orders.
    */
    public List<ApiOrderDTO> getOrders() {
        return orderRepository.findAll().stream()
            .map(order -> mapToApiOrderDTO(order))
            .collect(Collectors.toList());
    }

    // UPDATE

    /**
     * Updates the status of an order by its ID.
     *
     * This method performs the following steps:
     * 1. Retrieves the order by its ID. If the order does not exist, throws a ResourceNotFoundException.
     * 2. Updates the status of the order by setting the `name` field of the associated `OrderStatus` entity.
     * 3. Saves the updated order back to the database.
     * 4. Constructs an `ApiOrderStatusDTO` containing the updated status and returns it.
     *
     * @param orderId   The ID of the order to update.
     * @param newStatus The new status to set for the order.
     * @return An `ApiOrderStatusDTO` containing the updated status of the order.
     * @throws ResourceNotFoundException if the order with the given ID is not found.
     */
    public ApiOrderStatusDTO updateOrderStatus(int orderId, String newStatus) {

        // Validate newStatus
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new BadRequestException(null);
        }

        // Find the order by id or throw error
        Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order with id " + orderId + " not found"));

        // Find the new OrderStatus by name or throw error
        OrderStatus orderStatus = orderStatusService.findByName(newStatus)
        .orElseThrow(() -> new ResourceNotFoundException("Order status " + newStatus + " not found"));

        // Update order status
        order.setOrder_status(orderStatus);
        // Save order status
        orderRepository.save(order);

        // Create and return a DTO with updated status
        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus(newStatus);

        return statusDTO;
    }

   

    // HELPERS
    private int calculateTotalCost(List<ApiProductForOrderApiDTO> products) {
        return products.stream()
            .mapToInt(ApiProductForOrderApiDTO::getTotal_cost)
            .sum();
    }

     /**
     * Maps an `Order` entity to an `ApiOrderDTO`.
     *
     * This helper method performs the following steps:
     * 1. Maps the order's basic information (ID, customer, restaurant, courier details) to the corresponding fields in `ApiOrderDTO`.
     * 2. Retrieves and maps the products associated with the order to a list of `ApiProductForOrderApiDTO` objects.
     * 3. Calculates the total cost of the order based on the product details.
     * 4. Constructs and returns an `ApiOrderDTO` containing all the mapped details.
     *
     * @param order The `Order` entity to map.
     * @return An `ApiOrderDTO` containing the mapped details of the order.
     */
    private ApiOrderDTO mapToApiOrderDTO(Order order) {
        // Fetch addresses and convert to string
        String customerAddressString = addressService.findById(order.getCustomer().getAddressId())
        .map(addressService::convertToApiAddressDto)
        .map(ApiAddressDto::getFullAddress) // Assuming getFullAddress() returns a string
        .orElse(null);

    String restaurantAddressString = addressService.findById(order.getRestaurant().getAddressId())
        .map(addressService::convertToApiAddressDto)
        .map(ApiAddressDto::getFullAddress) // Assuming getFullAddress() returns a string
        .orElse(null);

    // Map order details to ApiOrderDTO including nested products
    List<ApiProductForOrderApiDTO> products = order.getProducts().stream()
        .map(this::mapToApiProductForOrderApiDTO)
        .collect(Collectors.toList());

    return ApiOrderDTO.builder()
        .id(order.getId())
        .customer_id(order.getCustomer().getId())
        .customer_name(order.getCustomer().getName())
        .customer_address(customerAddressString) // Use string for address
        .restaurant_id(order.getRestaurant().getId())
        .restaurant_name(order.getRestaurant().getName())
        .restaurant_address(restaurantAddressString) // Use string for address
        .status(order.getOrder_status().getName())
        .products(products)
        .total_cost(calculateTotalCost(products))
        .build();
    }

     /**
     * Maps a `ProductOrder` entity to an `ApiProductForOrderApiDTO`.
     *
     * This helper method performs the following steps:
     * 1. Maps the basic product details (ID, name, quantity, unit cost, total cost) from the `ProductOrder` entity to the corresponding fields in `ApiProductForOrderApiDTO`.
     * 2. Constructs and returns an `ApiProductForOrderApiDTO` containing the mapped product details.
     *
     * @param productOrder The `ProductOrder` entity to map.
     * @return An `ApiProductForOrderApiDTO` containing the mapped details of the product in the order.
     */
    private ApiProductForOrderApiDTO mapToApiProductForOrderApiDTO(ProductOrder productOrder) {
        return ApiProductForOrderApiDTO.builder()
            .id(productOrder.getProduct().getId())
            .product_name(productOrder.getProduct().getName())
            .quantity(productOrder.getProduct_quantity())
            .unit_cost(productOrder.getProduct_unit_cost())
            .total_cost(productOrder.getTotalCost())
            .build();
    }

    private Integer getRestaurantRating(int restaurantId) {
        List<Object[]> result = restaurantRepository.findRestaurantWithAverageRatingById(restaurantId);
        if (!result.isEmpty()) {
            Object[] data = result.get(0);
            // Assuming the rating is the fourth column (index 3) and is returned as a String
            Object ratingObj = data[3];
            Integer rating = 0; // Default to 0 if parsing fails
    
            if (ratingObj instanceof Number) {
                rating = ((Number) ratingObj).intValue();
            } else if (ratingObj instanceof String) {
                try {
                    rating = Integer.parseInt((String) ratingObj);
                } catch (NumberFormatException e) {
                    // Log or handle parsing error if needed
                    rating = 0; // Default to 0 if parsing fails
                }
            }
    
            return rating;
        }
        return 0; // Default to 0 if no rating is found
    }
}