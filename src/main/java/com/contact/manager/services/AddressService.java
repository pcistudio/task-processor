// src/main/java/com/contact/manager/services/AddressService.java
package com.contact.manager.services;

import com.contact.manager.entities.Address;
import com.contact.manager.model.AddressRequest;
import com.contact.manager.repositories.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address updateAddress(Long contactId, Long addressId, AddressRequest addressRequest) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setZipCode(addressRequest.getZipCode());
        address.setCountry(addressRequest.getCountry());

        return addressRepository.save(address);
    }

    public void deleteAddress(Long contactId, Long addressId) {
        addressRepository.deleteById(addressId);
    }
}