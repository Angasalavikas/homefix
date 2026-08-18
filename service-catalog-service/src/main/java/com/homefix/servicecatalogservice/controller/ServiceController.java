package com.homefix.servicecatalogservice.controller;

import com.homefix.servicecatalogservice.dto.ServiceItemRequest;
import com.homefix.servicecatalogservice.dto.ServiceItemResponse;
import com.homefix.servicecatalogservice.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ServiceController {

    private final CatalogService catalogService;

    @GetMapping("/services/search")
    public ResponseEntity<List<ServiceItemResponse>> searchServices(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String keyword) {
        List<ServiceItemResponse> responses = catalogService.searchServices(category, keyword);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceItemResponse>> getAllServices() {
        List<ServiceItemResponse> responses = catalogService.getAllServices();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceItemResponse> getServiceById(@PathVariable Long id) {
        ServiceItemResponse response = catalogService.getServiceById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceItemResponse> createService(@Valid @RequestBody ServiceItemRequest request) {
        ServiceItemResponse response = catalogService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceItemResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceItemRequest request) {
        ServiceItemResponse response = catalogService.updateService(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/services/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        catalogService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
