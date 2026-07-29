package com.homefix.paymentservice.controller;

import com.homefix.paymentservice.dto.InvoiceResponse;
import com.homefix.paymentservice.dto.PaymentRequest;
import com.homefix.paymentservice.dto.PaymentResponse;
import com.homefix.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /payments — Process a mock payment.
     * customerId is resolved from JWT.
     */
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        PaymentResponse response = paymentService.processPayment(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /payments/history — Payment history for the authenticated customer.
     */
    @GetMapping("/payments/history")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        List<PaymentResponse> responses = paymentService.getPaymentHistory(customerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /payments/{id}/invoice — Generate a structured invoice JSON.
     */
    @GetMapping("/payments/{id}/invoice")
    public ResponseEntity<InvoiceResponse> getInvoice(
            @PathVariable Long id,
            Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        InvoiceResponse invoice = paymentService.generateInvoice(id, customerId);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is up and running");
    }
}
