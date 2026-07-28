package com.homefix.customerservice.controller;

import com.homefix.customerservice.dto.CustomerResponse;
import com.homefix.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerService customerService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<CustomerResponse> getCustomerByUserId(@PathVariable Long userId) {
        CustomerResponse response = customerService.getProfile(userId);
        return ResponseEntity.ok(response);
    }
}
