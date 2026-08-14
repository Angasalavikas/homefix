package com.homefix.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body for the internal Feign call that updates ONLY a booking's
 * paymentStatus (UNPAID/PAID) in booking-service after a payment outcome.
 * The booking's lifecycle status is never touched by this call.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusUpdateRequest {

    @NotBlank(message = "paymentStatus is required")
    private String paymentStatus;
}
