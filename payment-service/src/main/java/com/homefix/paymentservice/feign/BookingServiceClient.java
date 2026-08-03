package com.homefix.paymentservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {

    @GetMapping("/internal/bookings/{id}")
    BookingResponse getBookingById(@PathVariable("id") Long id);
}
