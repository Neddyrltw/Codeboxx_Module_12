package com.rocketFoodDelivery.rocketFood.repository;

import com.rocketFoodDelivery.rocketFood.models.Address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    Optional<Address> findById(int id);

    List<Address> findAllByOrderByIdDesc();

    @Query(nativeQuery = true, value = "SELECT LAST_INSERT_ID() AS id")
    int getLastInsertedId();

    @Modifying
    @Query(value = "INSERT INTO address(streetAddress, city, postalCode) VALUES (:streetAddress, :city, :postalCode)", nativeQuery = true)
    void saveAddress(@Param("streetAddress") String streetAddress, @Param("city") String city, @Param("postalCode") String postalCode);
}
