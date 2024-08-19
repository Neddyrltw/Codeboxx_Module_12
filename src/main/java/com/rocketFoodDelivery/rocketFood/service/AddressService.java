package com.rocketFoodDelivery.rocketFood.service;

import com.rocketFoodDelivery.rocketFood.dtos.ApiAddressDto;
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Optional<Address> findById(int id){
        return addressRepository.findById(id);
    }

    public Optional<Integer> findLastAddressId() {
        List<Address> addresses = addressRepository.findAllByOrderByIdDesc();
        return addresses.isEmpty() ? Optional.empty() : Optional.of(addresses.get(0).getId());
    }
    
    public Address saveAddress(Address address){
        return addressRepository.save(address);
    }

    public ApiAddressDto convertToApiAddressDto(Address address) {
        return new ApiAddressDto(
            address.getId(),
            address.getStreetAddress(),
            address.getCity(),
            address.getPostalCode()
        );
    }

    public void delete(int id) {
        addressRepository.deleteById(id);
    }
}