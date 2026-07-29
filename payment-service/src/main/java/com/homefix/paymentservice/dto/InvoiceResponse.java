package com.homefix.paymentservice.dto;

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
public class InvoiceResponse {

    private Long invoiceId;
    private Long paymentId;
    private Long bookingId;
    private String serviceName;
    private String providerName;
    private String customerName;
    private LocalDateTime bookingDate;
    private String bookingAddress;
    private BigDecimal amount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private LocalDateTime transactionDate;
    private LocalDateTime generatedAt;
}
