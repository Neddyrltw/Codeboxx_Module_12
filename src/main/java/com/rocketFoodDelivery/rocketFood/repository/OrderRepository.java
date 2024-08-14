package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    /**
     * Finds an order by its ID.
     *
     * Executes a native SQL query to retrieve the order with the specified ID from the `orders` table.
     *
     * @param orderId The ID of the order to retrieve.
     * @return The `Order` object if found, otherwise `null`.
     */
    @Query(value = "SELECT * FROM orders WHERE id = :orderId", nativeQuery = true)
    Order findOrderById(@Param("orderId") int orderId);

    /**
     * Finds all orders associated with a specific customer ID.
     *
     * Executes a native SQL query to retrieve all orders that belong to the customer with the specified ID.
     *
     * @param customerId The ID of the customer whose orders are to be retrieved.
     * @return A list of `Order` objects associated with the specified customer.
     *         If no orders are found, an empty list is returned.
     */
    @Query(value = "SELECT * FROM orders WHERE customer_id = :customerId", nativeQuery = true)
    List<Order> findOrdersByCustomerId(@Param("customerId") int customerId);

    /**
     * Finds all orders associated with a specific restaurant ID.
     *
     * Executes a native SQL query to retrieve all orders that belong to the restaurant with the specified ID.
     *
     * @param restaurantId The ID of the restaurant whose orders are to be retrieved.
     * @return A list of `Order` objects associated with the specified restaurant.
     *         If no orders are found, an empty list is returned.
     */
    @Query(value = "SELECT * FROM orders WHERE restaurant_id = :restaurantId", nativeQuery = true)
    List<Order> findOrdersByRestaurantId(@Param("restaurantId") int restaurantId);

    /**
     * Finds all orders associated with a specific courier ID.
     *
     * Executes a native SQL query to retrieve all orders that belong to the courier with the specified ID.
     *
     * @param courierId The ID of the courier whose orders are to be retrieved.
     * @return A list of `Order` objects associated with the specified courier.
     *         If no orders are found, an empty list is returned.
     */
    @Query(value = "SELECT * FROM orders WHERE courier_id = :courierId", nativeQuery = true)
    List<Order> findOrdersByCourierId(@Param("courierId") int courierId);

    /**
     * Deletes an order by its ID.
     *
     * Executes a native SQL query to delete the order with the specified ID from the `orders` table.
     *
     * This method is marked as a modifying query, indicating that it performs a data update operation.
     * The transaction is managed at the method level, ensuring that the operation is atomic.
     *
     * @param orderId The ID of the order to delete.
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM orders WHERE id = :orderId", nativeQuery = true)
    void deleteOrderById(@Param("orderId") int orderId);

}
