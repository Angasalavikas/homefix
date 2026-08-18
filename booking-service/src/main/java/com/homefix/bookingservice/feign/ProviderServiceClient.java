package com.homefix.bookingservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "provider-service")
public interface ProviderServiceClient {

    @GetMapping("/internal/{id}")
    ProviderResponse getProviderById(@PathVariable("id") Long id);

    @GetMapping("/internal/by-user/{userId}")
    ProviderResponse getProviderByUserId(@PathVariable("userId") Long userId);
}
