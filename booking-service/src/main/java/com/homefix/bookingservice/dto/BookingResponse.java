package com.homefix.bookingservice.dto;

import com.homefix.bookingservice.entity.Booking;
import com.homefix.bookingservice.entity.BookingStatus;
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
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookingResponse fromBooking(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .customerId(booking.getCustomerId())
                .providerId(booking.getProviderId())
                .serviceId(booking.getServiceId())
                .bookingDate(booking.getBookingDate())
                .address(booking.getAddress())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
