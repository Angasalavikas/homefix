package com.homefix.paymentservice.dto;

import com.homefix.paymentservice.entity.Payment;
import com.homefix.paymentservice.entity.PaymentMethod;
import com.homefix.paymentservice.entity.PaymentStatus;
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
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private Long customerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private LocalDateTime transactionDate;
    private LocalDateTime createdAt;

    public static PaymentResponse fromPayment(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .method(payment.getMethod())
                .transactionDate(payment.getTransactionDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
