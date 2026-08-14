package com.homefix.paymentservice.feign;

import com.homefix.paymentservice.dto.PaymentStatusUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "booking-service")
public interface BookingServiceClient {

    @GetMapping("/internal/bookings/{id}")
    BookingResponse getBookingById(@PathVariable("id") Long id);

    /**
     * Update ONLY a booking's paymentStatus (UNPAID/PAID) after a successful
     * payment. Never touches the booking's lifecycle status — the provider
     * still sees the booking in "New Requests" until they accept it.
     */
    @PutMapping(value = "/internal/bookings/{id}/payment-status", consumes = "application/json")
    BookingResponse updateBookingPaymentStatus(@PathVariable("id") Long id,
                                               @RequestBody PaymentStatusUpdateRequest request);
}
