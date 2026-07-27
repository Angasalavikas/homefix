package com.homefix.customerservice.controller;

import com.homefix.customerservice.dto.*;
import com.homefix.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CustomerResponse response = customerService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(
            @Valid @RequestBody CustomerRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CustomerResponse response = customerService.createOrUpdateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<AddressResponse> addAddress(
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AddressResponse response = customerService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/me/addresses/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AddressResponse response = customerService.updateAddress(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        customerService.deleteAddress(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<List<AddressResponse>> getMyAddresses(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<AddressResponse> responses = customerService.getAddresses(userId);
        return ResponseEntity.ok(responses);
    }
}
