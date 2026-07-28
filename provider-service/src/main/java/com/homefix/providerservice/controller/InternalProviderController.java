package com.homefix.providerservice.controller;

import com.homefix.providerservice.dto.ProviderResponse;
import com.homefix.providerservice.service.ProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalProviderController {

    private final ProviderService providerService;

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
}
