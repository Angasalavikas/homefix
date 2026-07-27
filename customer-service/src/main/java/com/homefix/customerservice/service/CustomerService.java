package com.homefix.customerservice.service;

import com.homefix.customerservice.dto.*;
import com.homefix.customerservice.entity.Address;
import com.homefix.customerservice.entity.Customer;
import com.homefix.customerservice.repository.AddressRepository;
import com.homefix.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public CustomerResponse createOrUpdateProfile(Long userId, CustomerRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseGet(() -> Customer.builder()
                        .userId(userId)
                        .build());

        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }

        customer = customerRepository.save(customer);
        return CustomerResponse.fromCustomer(customer);
    }

    public CustomerResponse getProfile(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found. Please create your profile first."));
        return CustomerResponse.fromCustomer(customer);
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found. Please create your profile first."));

        Address address = Address.builder()
                .customer(customer)
                .label(request.getLabel())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zip(request.getZip())
                .isDefault(request.getIsDefault() != null && request.getIsDefault())
                .build();

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            customer.getAddresses().forEach(a -> a.setIsDefault(false));
        }

        customer.getAddresses().add(address);
        customerRepository.save(customer);

        return AddressResponse.fromAddress(address);
    }

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found."));

        Address address = customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (request.getLabel() != null) address.setLabel(request.getLabel());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getZip() != null) address.setZip(request.getZip());
        if (request.getIsDefault() != null) {
            if (Boolean.TRUE.equals(request.getIsDefault())) {
                customer.getAddresses().forEach(a -> a.setIsDefault(false));
            }
            address.setIsDefault(request.getIsDefault());
        }

        customerRepository.save(customer);
        return AddressResponse.fromAddress(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found."));

        Address address = customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Address not found"));

        customer.getAddresses().remove(address);
        customerRepository.save(customer);
    }

    public List<AddressResponse> getAddresses(Long userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found."));

        return customer.getAddresses().stream()
                .map(AddressResponse::fromAddress)
                .collect(Collectors.toList());
    }
}
