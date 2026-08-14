package com.homefix.bookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for the internal payment-status endpoint used by payment-service.
 * Updates ONLY {@code paymentStatus} (UNPAID/PAID) — never the booking's
 * lifecycle status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternalPaymentStatusUpdateRequest {

    @NotBlank(message = "paymentStatus is required")
    private String paymentStatus;
}
