package com.homefix.adminservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @GetMapping("/internal/payments/revenue")
    BigDecimal getTotalRevenue();
}
