package com.homefix.providerservice.controller;

import com.homefix.providerservice.dto.*;
import com.homefix.providerservice.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    /**
     * GET /available — Public listing of verified providers for the booking flow.
     */
    @GetMapping("/available")
    public ResponseEntity<List<ProviderResponse>> getAvailableProviders(
            @RequestParam Long serviceId) {

        List<ProviderResponse> responses =
                providerService.listVerifiedProviders(serviceId);

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/profile")
    public ResponseEntity<ProviderResponse> registerProfile(
            @Valid @RequestBody ProviderRegistrationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ProviderResponse response = providerService.registerProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<ProviderResponse> getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ProviderResponse response = providerService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/availability")
    public ResponseEntity<ProviderResponse> updateAvailability(
            @Valid @RequestBody AvailabilityUpdateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ProviderResponse response = providerService.updateAvailability(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<ProviderResponse> getProviderById(@PathVariable Long id) {
        ProviderResponse response = providerService.getProviderById(id);
        return ResponseEntity.ok(response);
    }
}
