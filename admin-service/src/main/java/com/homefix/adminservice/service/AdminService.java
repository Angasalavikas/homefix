package com.homefix.adminservice.service;

import com.homefix.adminservice.dto.*;
import com.homefix.adminservice.feign.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final CustomerServiceClient customerServiceClient;
    private final ProviderServiceClient providerServiceClient;
    private final BookingServiceClient bookingServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    /**
     * Aggregate dashboard counts from all services via Feign calls.
     * Individual service failures are handled gracefully with fallback values.
     */
    public DashboardResponse getDashboard() {
        long totalCustomers = fetchWithFallback(customerServiceClient::getCustomerCount, 0L);
        long totalProviders = fetchWithFallback(providerServiceClient::getProviderCount, 0L);

        Map<String, Long> bookingsByStatus = fetchWithFallback(
                () -> bookingServiceClient.getBookingStats(),
                Collections.emptyMap()
        );

        BigDecimal totalRevenue = fetchWithFallback(
                () -> paymentServiceClient.getTotalRevenue(),
                BigDecimal.ZERO
        );

        return DashboardResponse.builder()
                .totalCustomers(totalCustomers)
                .totalProviders(totalProviders)
                .bookingsByStatus(bookingsByStatus)
                .totalRevenue(totalRevenue)
                .build();
    }

    public List<AdminCustomerResponse> getAllCustomers() {
        return fetchWithFallback(customerServiceClient::getAllCustomers, Collections.emptyList());
    }

    public List<AdminProviderResponse> getAllProviders() {
        return fetchWithFallback(providerServiceClient::getAllProviders, Collections.emptyList());
    }

    public List<AdminBookingResponse> getAllBookings() {
        return fetchWithFallback(bookingServiceClient::getAllBookings, Collections.emptyList());
    }

    public void verifyProvider(Long providerId) {
        try {
            providerServiceClient.verifyProvider(providerId);
            log.info("Provider {} verified successfully", providerId);
        } catch (FeignException e) {
            log.error("Failed to verify provider {}: {}", providerId, e.getMessage());
            throw new RuntimeException("Failed to verify provider. Provider service error.");
        }
    }

    private <T> T fetchWithFallback(java.util.function.Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (FeignException e) {
            log.warn("Feign call failed, using fallback value: {}", e.getMessage());
            return fallback;
        }
    }
}
