package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.dtos.ApiProductForOrderApiDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.ProductOrder;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AddressService addressService;

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
        // Find the order by id or throw error
        Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order with id " + orderId + " not found"));

        // Update order status
        order.getOrder_status().setName(newStatus);

        // Save order status
        orderRepository.save(order);

        // Create and return a DTO with updated status
        ApiOrderStatusDTO statusDTO = new ApiOrderStatusDTO();
        statusDTO.setStatus(newStatus);

        return statusDTO;
    }

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
        // 1. Validate input
        if (type == null || id <= 0) {
            throw new BadRequestException("Invalid or missing parameters");
        }

        // 2. Fetch orders based on type
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

        // 3. Check if orders were found
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for the given criteria");
        }

        // 4. Assemble response
        return orders.stream()
            .map(this::mapToApiOrderDTO)
            .collect(Collectors.toList());
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

        // Map order details to ApiOrderDTO including nested products
        List<ApiProductForOrderApiDTO> products = order.getProducts().stream()
            .map(this::mapToApiProductForOrderApiDTO)
            .collect(Collectors.toList());

            return ApiOrderDTO.builder()
            .id(order.getId())
            .customer_id(order.getCustomer().getId())
            .customer_name(order.getCustomer().getName())
            .customer_address(addressService.convertToApiAddressDto(order.getCustomer().getAddress()).toString()) // Convert to String
            .restaurant_id(order.getRestaurant().getId())
            .restaurant_name(order.getRestaurant().getName())
            .restaurant_address(addressService.convertToApiAddressDto(order.getRestaurant().getAddress()).toString()) // Convert to String
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

    private int calculateTotalCost(List<ApiProductForOrderApiDTO> products) {
        return products.stream()
            .mapToInt(ApiProductForOrderApiDTO::getTotal_cost)
            .sum();
    }
}