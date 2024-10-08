package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    Optional<Restaurant> findByUserEntityId(int id);
    List<Restaurant> findAll();

    /**
     * Inserts a new restaurant into the database.
     *
     * @param userId      The ID of the user associated with the restaurant.
     * @param addressId   The ID of the address associated with the restaurant.
     * @param name        The name of the restaurant.
     * @param priceRange  The price range of the restaurant.
     * @param phone       The phone number of the restaurant.
     * @param email       The email address of the restaurant.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO restaurants (user_id, address_id, name, price_range, phone, email) VALUES (:userId, :addressId, :name, :priceRange, :phone, :email)", nativeQuery = true)
    void saveRestaurant(@Param("userId") int userId,
                         @Param("addressId") int addressId,
                         @Param("name") String name,
                         @Param("priceRange") int priceRange,
                         @Param("phone") String phone,
                         @Param("email") String email);

    /**
     * Finds a restaurant by its ID along with the calculated average rating rounded up to the ceiling.
     *
     * @param restaurantId The ID of the restaurant to retrieve.
     * @return A list of Object arrays representing the selected columns from the query result.
     *         Each Object array corresponds to the restaurant's information.
     *         An empty list is returned if no restaurant is found with the specified ID.
     */
    @Query(nativeQuery = true, value =
        "SELECT r.id, r.name, r.price_range, COALESCE(CEIL(SUM(o.restaurant_rating) / NULLIF(COUNT(o.id), 0)), 0) AS rating " +
        "FROM restaurants r " +
        "LEFT JOIN orders o ON r.id = o.restaurant_id " +
        "WHERE r.id = :restaurantId " +
        "GROUP BY r.id")
    List<Object[]> findRestaurantWithAverageRatingById(@Param("restaurantId") int restaurantId);
    
    /**
     * Finds restaurants based on the provided rating and price range.
     *
     * Executes a native SQL query that retrieves restaurants with their information, including a calculated
     * average rating rounded up to the ceiling.
     *
     * @param rating     The minimum rounded-up average rating of the restaurants. (Optional)
     * @param priceRange The price range of the restaurants. (Optional)
     * @return A list of Object arrays representing the selected columns from the query result.
     *         Each Object array corresponds to a restaurant's information.
     *         An empty list is returned if no restaurant is found with the specified ID.
     */
    @Query(nativeQuery = true, value =
        "SELECT * FROM (" +
        "   SELECT r.id, r.name, r.price_range, COALESCE(CEIL(SUM(o.restaurant_rating) / NULLIF(COUNT(o.id), 0)), 0) AS rating " +
        "   FROM restaurants r " +
        "   LEFT JOIN orders o ON r.id = o.restaurant_id " +
        "   WHERE (:priceRange IS NULL OR r.price_range = :priceRange) " +
        "   GROUP BY r.id" +
        ") AS result " +
        "WHERE (:rating IS NULL OR result.rating = :rating)")
    List<Object[]> findRestaurantsByRatingAndPriceRange(@Param("rating") Integer rating, @Param("priceRange") Integer priceRange);

    /**
     * Retrieves the ID of the last inserted restaurant.
     *
     * @return An Optional containing the ID of the last inserted restaurant, or an empty Optional if no restaurant has been inserted.
     */
    @Query(nativeQuery = true, value = "SELECT LAST_INSERT_ID()")
    Optional<Integer> findLastInsertedId();

    /**
     * Finds a restaurant by its ID.
     *
     * @param id The ID of the restaurant to find.
     * @return An Optional containing the found restaurant, or an empty Optional if no restaurant with the given ID exists.
     */
    @Query(nativeQuery = true, value = "SELECT * FROM restaurants WHERE id = :id")
    Optional<Restaurant> findById(@Param("id") int id);

    /**
     * Updates the details of an existing restaurant in the database.
     *
     * Executes a native SQL query that updates the `name`, `price_range`, and `phone` fields of the 
     * restaurant record identified by the given `restaurantId`.
     *
     * This method is marked as a modifying query, indicating that it performs a data update operation.
     * The transaction is managed at the method level, ensuring that the operation is atomic.
     *
     * @param restaurantId The ID of the restaurant to update. (Required)
     * @param name         The new name for the restaurant. (Required)
     * @param priceRange   The new price range for the restaurant. (Required)
     * @param phone        The new phone number for the restaurant. (Required)
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE restaurants SET name = :name, price_range = :priceRange, phone = :phone WHERE id = :restaurantId", nativeQuery = true)
    void updateRestaurant(@Param("restaurantId") int restaurantId, @Param("name") String name, @Param("priceRange") int priceRange, @Param("phone") String phone);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM restaurants WHERE id = :restaurantId", nativeQuery = true)
    void deleteRestaurantById(@Param("restaurantId") int restaurantId);    
}

