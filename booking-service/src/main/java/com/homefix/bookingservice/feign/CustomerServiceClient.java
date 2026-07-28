package com.homefix.bookingservice.feign;

import com.homefix.bookingservice.feign.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping("/internal/users/{userId}")
    CustomerResponse getCustomerByUserId(@PathVariable("userId") Long userId);
}
