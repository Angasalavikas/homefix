package com.homefix.paymentservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-catalog-service")
public interface ServiceCatalogClient {

    @GetMapping("/internal/services/{id}")
    ServiceResponse getServiceById(@PathVariable("id") Long id);
}
