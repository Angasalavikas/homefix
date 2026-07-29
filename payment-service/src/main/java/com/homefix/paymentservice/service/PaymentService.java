package com.homefix.paymentservice.service;

import com.homefix.paymentservice.dto.InvoiceResponse;
import com.homefix.paymentservice.dto.PaymentRequest;
import com.homefix.paymentservice.dto.PaymentResponse;
import com.homefix.paymentservice.entity.Payment;
import com.homefix.paymentservice.entity.PaymentStatus;
import com.homefix.paymentservice.exception.PaymentException;
import com.homefix.paymentservice.feign.*;
import com.homefix.paymentservice.repository.PaymentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;
    private final ProviderServiceClient providerServiceClient;
    private final ServiceCatalogClient serviceCatalogClient;
    private final NotificationServiceClient notificationServiceClient;

    /**
     * Process a mock payment. Always succeeds for now but structured so a real
     * payment gateway can be swapped in later.
     */
    @Transactional
    public PaymentResponse processPayment(Long customerId, PaymentRequest request) {
        // Validate booking exists via Feign
        BookingResponse booking;
        try {
            booking = bookingServiceClient.getBookingById(request.getBookingId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new PaymentException("Booking not found with ID: " + request.getBookingId());
            }
            throw new PaymentException("Booking service is currently unavailable");
        }

        // Verify booking belongs to this customer
        if (!booking.getCustomerId().equals(customerId)) {
            throw new PaymentException("Booking does not belong to the authenticated customer");
        }

        // --- MOCK PAYMENT GATEWAY ---
        // Simulate payment processing (always succeeds for now).
        // To swap in a real gateway, replace this block with the real integration.
        PaymentStatus paymentStatus = simulatePaymentGateway(request);
        // --- END MOCK ---

        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .customerId(customerId)
                .amount(request.getAmount())
                .status(paymentStatus)
                .method(request.getMethod())
                .transactionDate(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        // If payment succeeded, trigger notification
        if (paymentStatus == PaymentStatus.SUCCESS) {
            try {
                String message = String.format(
                        "Payment of ₹%.2f for booking #%d was successful via %s.",
                        request.getAmount(), request.getBookingId(), request.getMethod()
                );
                notificationServiceClient.createNotification(NotificationRequest.builder()
                        .recipientId(customerId)
                        .type("PAYMENT_SUCCESS")
                        .message(message)
                        .build());
                log.info("Payment success notification sent for booking {}", request.getBookingId());
            } catch (FeignException e) {
                log.warn("Failed to send payment success notification: {}", e.getMessage());
            }
        }

        return PaymentResponse.fromPayment(payment);
    }

    /**
     * Mock payment gateway simulation. Always returns SUCCESS.
     * Replace this method with a real gateway integration.
     */
    private PaymentStatus simulatePaymentGateway(PaymentRequest request) {
        // Real gateway integration would go here:
        // e.g., call Stripe/Razorpay API with request.getAmount(), request.getMethod()
        log.info("Mock payment processed: bookingId={}, amount={}, method={}",
                request.getBookingId(), request.getAmount(), request.getMethod());
        return PaymentStatus.SUCCESS;
    }

    /**
     * Get payment history for the authenticated customer.
     */
    public List<PaymentResponse> getPaymentHistory(Long customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(PaymentResponse::fromPayment)
                .collect(Collectors.toList());
    }

    /**
     * Generate an invoice for a given payment.
     * Enriches payment data with booking, provider, and service details via Feign.
     */
    public InvoiceResponse generateInvoice(Long paymentId, Long customerId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));

        if (!payment.getCustomerId().equals(customerId)) {
            throw new PaymentException("Payment does not belong to the authenticated customer");
        }

        // Enrich with booking details
        BookingResponse booking;
        try {
            booking = bookingServiceClient.getBookingById(payment.getBookingId());
        } catch (FeignException e) {
            log.warn("Failed to fetch booking details for invoice: {}", e.getMessage());
            return buildPartialInvoice(payment);
        }

        // Enrich with service details
        String serviceName = "N/A";
        try {
            ServiceResponse service = serviceCatalogClient.getServiceById(booking.getServiceId());
            serviceName = service.getName();
        } catch (FeignException e) {
            log.warn("Failed to fetch service details for invoice: {}", e.getMessage());
        }

        // Enrich with provider details
        String providerName = "N/A";
        try {
            ProviderResponse provider = providerServiceClient.getProviderById(booking.getProviderId());
            providerName = provider.getName();
        } catch (FeignException e) {
            log.warn("Failed to fetch provider details for invoice: {}", e.getMessage());
        }

        return InvoiceResponse.builder()
                .invoiceId(payment.getId())
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .serviceName(serviceName)
                .providerName(providerName)
                .customerName(booking.getCustomerName())
                .bookingDate(booking.getBookingDate())
                .bookingAddress(booking.getAddress())
                .amount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .paymentMethod(payment.getMethod())
                .transactionDate(payment.getTransactionDate())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private InvoiceResponse buildPartialInvoice(Payment payment) {
        return InvoiceResponse.builder()
                .invoiceId(payment.getId())
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .serviceName("Unavailable")
                .providerName("Unavailable")
                .customerName("Unavailable")
                .amount(payment.getAmount())
                .paymentStatus(payment.getStatus())
                .paymentMethod(payment.getMethod())
                .transactionDate(payment.getTransactionDate())
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
