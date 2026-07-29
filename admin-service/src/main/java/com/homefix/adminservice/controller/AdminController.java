package com.homefix.adminservice.controller;

import com.homefix.adminservice.dto.*;
import com.homefix.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /admin/dashboard — Aggregate counts from all services.
     */
    @GetMapping("/admin/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        DashboardResponse dashboard = adminService.getDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET /admin/customers — List all customers.
     */
    @GetMapping("/admin/customers")
    public ResponseEntity<List<AdminCustomerResponse>> getAllCustomers() {
        List<AdminCustomerResponse> customers = adminService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * GET /admin/providers — List all providers.
     */
    @GetMapping("/admin/providers")
    public ResponseEntity<List<AdminProviderResponse>> getAllProviders() {
        List<AdminProviderResponse> providers = adminService.getAllProviders();
        return ResponseEntity.ok(providers);
    }

    /**
     * GET /admin/bookings — List all bookings.
     */
    @GetMapping("/admin/bookings")
    public ResponseEntity<List<AdminBookingResponse>> getAllBookings() {
        List<AdminBookingResponse> bookings = adminService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * PUT /admin/providers/{id}/verify — Verify a provider.
     */
    @PutMapping("/admin/providers/{id}/verify")
    public ResponseEntity<String> verifyProvider(@PathVariable Long id) {
        adminService.verifyProvider(id);
        return ResponseEntity.ok("Provider " + id + " verified successfully");
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Admin Service is up and running");
    }
}
