package com.homefix.adminservice.feign;

import com.homefix.adminservice.dto.AdminBookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {

    @GetMapping("/internal/bookings")
    List<AdminBookingResponse> getAllBookings();

    @GetMapping("/internal/bookings/stats")
    Map<String, Long> getBookingStats();
}
