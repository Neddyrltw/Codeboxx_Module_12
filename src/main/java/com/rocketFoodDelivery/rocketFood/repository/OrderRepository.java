package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Find an order by ID
    @Query(value = "SELECT * FROM orders WHERE id = :orderId", nativeQuery = true)
    Order findOrderById(@Param("orderId") int orderId);


    // // TODO
    // @Modifying
    // @Transactional
    // @Query(nativeQuery = true)
    // // value = "TODO Write SQL query here")
    // void deleteOrderById(@Param("orderId") int orderId);
}
