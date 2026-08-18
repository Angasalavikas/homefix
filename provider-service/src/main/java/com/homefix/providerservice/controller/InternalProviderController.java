package com.homefix.providerservice.controller;

import com.homefix.providerservice.dto.ProviderResponse;
import com.homefix.providerservice.entity.Provider;
import com.homefix.providerservice.entity.VerificationStatus;
import com.homefix.providerservice.repository.ProviderRepository;
import com.homefix.providerservice.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalProviderController {

    private final ProviderService providerService;
    private final ProviderRepository providerRepository;

    @GetMapping("/{id}")
    public ResponseEntity<ProviderResponse> getProviderById(@PathVariable Long id) {
        ProviderResponse response = providerService.getProviderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<ProviderResponse> getProviderByUserId(@PathVariable Long userId) {
        ProviderResponse response = providerService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /internal/providers — List all providers for admin.
     */
    @GetMapping("/providers")
    public ResponseEntity<List<ProviderResponse>> getAllProviders() {
        List<ProviderResponse> providers = providerRepository.findAll().stream()
                .map(ProviderResponse::fromProvider)
                .collect(Collectors.toList());
        return ResponseEntity.ok(providers);
    }

    /**
     * GET /internal/providers/count — Total provider count for admin dashboard.
     */
    @GetMapping("/providers/count")
    public ResponseEntity<Long> getProviderCount() {
        long count = providerRepository.count();
        return ResponseEntity.ok(count);
    }

    /**
     * PUT /internal/providers/{id}/verify — Admin verifies a provider.
     */
    @PutMapping("/providers/{id}/verify")
    public ResponseEntity<String> verifyProvider(@PathVariable Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found with ID: " + id));
        provider.setVerificationStatus(VerificationStatus.VERIFIED);
        providerRepository.save(provider);
        return ResponseEntity.ok("Provider " + id + " verified successfully");
    }
}
