package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findById(int id);
    List <Product> findAll();
    List<Product> findByRestaurantId(int restaurantId);

/**
 * Finds a list of products associated with a specific restaurant by the restaurant's ID.
 *
 * @param restaurantId The ID of the restaurant whose products are to be retrieved.
 * @return A list of Product objects representing the products associated with the specified restaurant.
 *         An empty list is returned if no products are found for the specified restaurant.
 */
@Query(nativeQuery = true, value=
    "SELECT p.id, p.name, p.cost, p.description, p.restaurant_id " +
    "FROM products p " +
    "WHERE p.restaurant_id = :restaurantId")
List<Object[]> findProductsByRestaurantId(@Param("restaurantId") int restaurantId);

/**
 * Deletes all products associated with a specific restaurant by the restaurant's ID.
 *
 * @param restaurantId The ID of the restaurant whose products are to be deleted.
 */
@Modifying
@Transactional
@Query(nativeQuery = true, value =
    "DELETE FROM products " +
    "WHERE restaurant_id = :restaurantId")
void deleteProductsByRestaurantId(@Param("restaurantId") int restaurantId);
}
