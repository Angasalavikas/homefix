package com.homefix.customerservice.controller;

import com.homefix.customerservice.dto.CustomerRequest;
import com.homefix.customerservice.dto.CustomerResponse;
import com.homefix.customerservice.entity.Customer;
import com.homefix.customerservice.repository.CustomerRepository;
import com.homefix.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    @GetMapping("/users/{userId}")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId) {
        CustomerResponse response = customerService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/customers")
    public ResponseEntity<CustomerResponse> createCustomer(
            @RequestBody CustomerRequest request) {

        CustomerResponse response =
                customerService.createOrUpdateProfile(request.getUserId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * GET /internal/customers — List all customers for admin.
     */
    @GetMapping("/customers")
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customers = customerRepository.findAll().stream()
                .map(CustomerResponse::fromCustomer)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    /**
     * GET /internal/customers/count — Total customer count for admin dashboard.
     */
    @GetMapping("/customers/count")
    public ResponseEntity<Long> getCustomerCount() {
        long count = customerRepository.count();
        return ResponseEntity.ok(count);
    }
}
