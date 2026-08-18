package com.homefix.paymentservice.service;

import com.homefix.paymentservice.dto.*;
import com.homefix.paymentservice.entity.Payment;
import com.homefix.paymentservice.entity.PaymentStatus;
import com.homefix.paymentservice.exception.PaymentException;
import com.homefix.paymentservice.feign.*;
import com.homefix.paymentservice.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Value("${razorpay.key-id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret:}")
    private String razorpayKeySecret;

    // ===================== RAZORPAY ORDER =====================

    /**
     * Create a Razorpay Order for a booking and persist a PENDING payment record
     * that the frontend uses to open Razorpay Checkout.
     */
    @Transactional
    public CreateOrderResponse createOrder(Long customerId, CreateOrderRequest request) {
        // Validate booking exists and belongs to the authenticated customer
        fetchBooking(customerId, request.getBookingId());

        RazorpayClient client = razorpayClient();

        JSONObject orderOptions = new JSONObject();
        orderOptions.put("amount", amountToPaise(request.getAmount()));
        orderOptions.put("currency", "INR");
        orderOptions.put("receipt", "booking_" + request.getBookingId());
        orderOptions.put("notes", new JSONObject()
                .put("bookingId", request.getBookingId())
                .put("customerId", customerId));

        final String orderId;
        try {
            Order order = client.orders.create(orderOptions);
            orderId = order.get("id");
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for booking {}: {}", request.getBookingId(), e.getMessage());
            throw new PaymentException("Could not create the payment order with Razorpay. Please try again.");
        }

        // Persist a PENDING payment linked to the Razorpay order
        Payment payment = Payment.builder()
                .bookingId(request.getBookingId())
                .customerId(customerId)
                .amount(request.getAmount())
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .razorpayOrderId(orderId)
                .transactionDate(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        log.info("Razorpay order {} created for booking {}", orderId, request.getBookingId());
        return CreateOrderResponse.builder()
                .orderId(orderId)
                .amount(request.getAmount())
                .currency("INR")
                .razorpayKeyId(razorpayKeyId)
                .build();
    }

    // ===================== VERIFY =====================

    /**
     * Verify the payment signature server-side (never trust the frontend's
     * success callback alone), then mark the payment SUCCESS/FAILED and — on
     * success — notify the customer and mark the booking PAID via Feign.
     */
    @Transactional
    public PaymentResponse verifyPayment(Long customerId, VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentException(
                        "No payment found for Razorpay order: " + request.getRazorpayOrderId()));

        if (!payment.getCustomerId().equals(customerId)) {
            throw new PaymentException("Payment does not belong to the authenticated customer");
        }

        boolean verified;
        try {
            verified = Utils.verifyPaymentSignature(new JSONObject()
                    .put("razorpay_order_id", request.getRazorpayOrderId())
                    .put("razorpay_payment_id", request.getRazorpayPaymentId())
                    .put("razorpay_signature", request.getRazorpaySignature()),
                    razorpayKeySecret);
        } catch (RazorpayException e) {
            log.warn("Razorpay signature verification failed for order {}: {}",
                    request.getRazorpayOrderId(), e.getMessage());
            verified = false;
        }

        if (!verified) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentException("Payment verification failed. Please try again.");
        }

        // SUCCESS path
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setMethod(fetchRazorpayMethod(request.getRazorpayPaymentId()));
        payment.setTransactionDate(LocalDateTime.now());
        paymentRepository.save(payment);

        sendPaymentSuccessNotification(payment);
        markBookingPaid(payment.getBookingId());

        return PaymentResponse.fromPayment(payment);
    }

    // ===================== HISTORY & INVOICE =====================

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

    // ===================== HELPERS =====================

    private BookingResponse fetchBooking(Long customerId, Long bookingId) {
        BookingResponse booking;
        try {
            booking = bookingServiceClient.getBookingById(bookingId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                throw new PaymentException("Booking not found with ID: " + bookingId);
            }
            throw new PaymentException("Booking service is currently unavailable");
        }

        if (!booking.getCustomerId().equals(customerId)) {
            throw new PaymentException("Booking does not belong to the authenticated customer");
        }
        return booking;
    }

    private RazorpayClient razorpayClient() {
        if (razorpayKeyId == null || razorpayKeyId.isBlank()
                || razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            throw new PaymentException(
                    "Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET "
                            + "environment variables (see SETUP.md).");
        }
        try {
            return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        } catch (RazorpayException e) {
            throw new PaymentException("Could not initialize the Razorpay client: " + e.getMessage());
        }
    }

    /** Razorpay amounts are in the smallest currency unit (paise for INR). */
    private long amountToPaise(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private void sendPaymentSuccessNotification(Payment payment) {
        try {
            String message = String.format(
                    "Payment of ₹%.2f for booking #%d was successful.",
                    payment.getAmount(), payment.getBookingId());
            notificationServiceClient.createNotification(NotificationRequest.builder()
                    .recipientId(payment.getCustomerId())
                    .type("PAYMENT_SUCCESS")
                    .message(message)
                    .build());
            log.info("Payment success notification sent for booking {}", payment.getBookingId());
        } catch (FeignException e) {
            log.warn("Failed to send payment success notification: {}", e.getMessage());
        }
    }

    /**
     * Best-effort update of the booking's paymentStatus to PAID via the
     * internal payment-status endpoint. This NEVER touches the booking's
     * lifecycle status — a paid booking stays PENDING until the provider
     * accepts it through the normal status-update flow.
     */
    private void markBookingPaid(Long bookingId) {
        try {
            bookingServiceClient.updateBookingPaymentStatus(bookingId,
                    PaymentStatusUpdateRequest.builder().paymentStatus("PAID").build());
            log.info("Booking {} paymentStatus updated to PAID (lifecycle status unchanged)", bookingId);
        } catch (FeignException e) {
            log.warn("Failed to update booking {} paymentStatus to PAID: {}", bookingId, e.getMessage());
        }
    }

    /** Best-effort capture of the Razorpay payment method (card, upi, netbanking, …). */
    private String fetchRazorpayMethod(String razorpayPaymentId) {
        try {
            com.razorpay.Payment razorpayPayment = razorpayClient().payments.fetch(razorpayPaymentId);
            Object method = razorpayPayment.get("method");
            return method == null ? null : method.toString().toLowerCase();
        } catch (Exception e) {
            log.warn("Could not fetch Razorpay payment method for {}: {}", razorpayPaymentId, e.getMessage());
            return null;
        }
    }
}
