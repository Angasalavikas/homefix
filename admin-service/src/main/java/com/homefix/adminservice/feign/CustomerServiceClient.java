package com.homefix.adminservice.feign;

import com.homefix.adminservice.dto.AdminCustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping("/internal/customers")
    List<AdminCustomerResponse> getAllCustomers();

    @GetMapping("/internal/customers/count")
    long getCustomerCount();
}
