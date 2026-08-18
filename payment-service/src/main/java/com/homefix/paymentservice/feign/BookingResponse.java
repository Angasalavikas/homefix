package com.homefix.paymentservice.feign;

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
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private String customerAddress;
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
