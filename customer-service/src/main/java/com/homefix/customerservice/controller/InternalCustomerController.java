package com.homefix.customerservice.controller;

import com.homefix.customerservice.dto.CustomerResponse;
import com.homefix.customerservice.entity.Customer;
import com.homefix.customerservice.repository.CustomerRepository;
import com.homefix.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
