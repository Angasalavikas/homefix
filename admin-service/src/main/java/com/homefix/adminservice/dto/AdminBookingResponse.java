package com.homefix.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long providerId;
    private String providerName;
    private Long serviceId;
    private String serviceName;
    private BigDecimal servicePrice;
    private LocalDateTime bookingDate;
    private String address;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
