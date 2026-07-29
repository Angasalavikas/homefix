package com.homefix.adminservice.feign;

import com.homefix.adminservice.dto.AdminProviderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "provider-service")
public interface ProviderServiceClient {

    @GetMapping("/internal/providers")
    List<AdminProviderResponse> getAllProviders();

    @GetMapping("/internal/providers/count")
    long getProviderCount();

    @PutMapping("/internal/providers/{id}/verify")
    void verifyProvider(@PathVariable("id") Long id);
}
