package com.homefix.authservice.feign;

import com.homefix.authservice.dto.CustomerRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @PostMapping("/internal/customers")
    void createCustomer(@RequestBody CustomerRequest request);

}