package com.homefix.bookingservice.service;

import com.homefix.bookingservice.dto.BookingRequest;
import com.homefix.bookingservice.dto.BookingResponse;
import com.homefix.bookingservice.dto.StatusUpdateRequest;
import com.homefix.bookingservice.entity.Booking;
import com.homefix.bookingservice.entity.BookingStatus;
import com.homefix.bookingservice.exception.BookingException;
import com.homefix.bookingservice.feign.*;
import com.homefix.bookingservice.repository.BookingRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.homefix.bookingservice.service.FeignUtils.callWithFallback;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CustomerServiceClient customerServiceClient;
    private final ProviderServiceClient providerServiceClient;
    private final ServiceCatalogClient serviceCatalogClient;

    /**
     * Valid status transitions:
     * PENDING     -> ACCEPTED, CANCELLED
     * ACCEPTED    -> ON_THE_WAY, CANCELLED
     * ON_THE_WAY  -> STARTED
     * STARTED     -> COMPLETED
     * COMPLETED   -> (terminal)
     * CANCELLED   -> (terminal)
     */
    private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS = new HashMap<>();

    static {
        VALID_TRANSITIONS.put(BookingStatus.PENDING, Set.of(BookingStatus.ACCEPTED, BookingStatus.CANCELLED));
        VALID_TRANSITIONS.put(BookingStatus.ACCEPTED, Set.of(BookingStatus.ON_THE_WAY, BookingStatus.CANCELLED));
        VALID_TRANSITIONS.put(BookingStatus.ON_THE_WAY, Set.of(BookingStatus.STARTED));
        VALID_TRANSITIONS.put(BookingStatus.STARTED, Set.of(BookingStatus.COMPLETED));
        VALID_TRANSITIONS.put(BookingStatus.COMPLETED, Collections.emptySet());
        VALID_TRANSITIONS.put(BookingStatus.CANCELLED, Collections.emptySet());
    }

    // ===================== CREATE =====================

    @Transactional
    public BookingResponse createBooking(Long customerId, BookingRequest request) {
        // Validate customer exists via Feign
        CustomerResponse customer = callWithFallback(
                () -> customerServiceClient.getCustomerByUserId(customerId),
                "Customer with userId " + customerId + " not found"
        );

        // Validate provider exists and is available/verified
        ProviderResponse provider = callWithFallback(
                () -> providerServiceClient.getProviderById(request.getProviderId()),
                "Provider with ID " + request.getProviderId() + " not found"
        );

        if (!"AVAILABLE".equalsIgnoreCase(provider.getAvailability())) {
            throw new BookingException("Provider is not available for booking");
        }
        if (!"VERIFIED".equalsIgnoreCase(provider.getVerificationStatus())) {
            throw new BookingException("Provider is not verified");
        }

        // Validate service exists via Feign
        ServiceResponse service = callWithFallback(
                () -> serviceCatalogClient.getServiceById(request.getServiceId()),
                "Service with ID " + request.getServiceId() + " not found"
        );

        // Build booking
        Booking booking = Booking.builder()
                .customerId(customerId)
                .providerId(request.getProviderId())
                .serviceId(request.getServiceId())
                .bookingDate(request.getBookingDate())
                .address(request.getAddress())
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);

        return enrichResponse(booking, customer, provider, service);
    }

    // ===================== LIST =====================

    public List<BookingResponse> listBookings(Long userId, String role, String statusFilter) {
        List<Booking> bookings;

        if ("provider".equalsIgnoreCase(role)) {
            // Resolve JWT userId -> provider DB id
            Long providerId = resolveProviderDbId(userId);
            bookings = (statusFilter != null)
                    ? bookingRepository.findByProviderIdAndStatusOrderByCreatedAtDesc(providerId, parseStatus(statusFilter))
                    : bookingRepository.findByProviderIdOrderByCreatedAtDesc(providerId);
        } else {
            // Customer — JWT userId IS the customerId
            bookings = (statusFilter != null)
                    ? bookingRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(userId, parseStatus(statusFilter))
                    : bookingRepository.findByCustomerIdOrderByCreatedAtDesc(userId);
        }

        return bookings.stream()
                .map(this::enrichResponse)
                .collect(Collectors.toList());
    }

    // ===================== UPDATE STATUS (Provider or Admin) =====================

    @Transactional
    public BookingResponse updateStatus(Long bookingId, Long userId, boolean isAdmin, StatusUpdateRequest request) {
        Booking booking;

        if (isAdmin) {
            // Admin can update any booking
            booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new BookingException("Booking not found with ID: " + bookingId));
        } else {
            // Provider — resolve JWT userId -> provider DB id and verify ownership
            Long providerId = resolveProviderDbId(userId);
            booking = bookingRepository.findByIdAndProviderId(bookingId, providerId)
                    .orElseThrow(() -> new BookingException("Booking not found or not associated with this provider"));
        }

        BookingStatus newStatus = parseStatus(request.getStatus());
        validateTransition(booking.getStatus(), newStatus);

        booking.setStatus(newStatus);
        booking = bookingRepository.save(booking);

        return enrichResponse(booking);
    }

    // ===================== CANCEL (Customer) =====================

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long customerId) {
        Booking booking = bookingRepository.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BookingException("Booking not found or not associated with this customer"));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new BookingException(
                    "Booking can only be cancelled from PENDING or ACCEPTED status. Current status: " + booking.getStatus()
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        return enrichResponse(booking);
    }

    // ===================== PROVIDER IDENTITY RESOLUTION =====================

    /**
     * Resolve the JWT auth userId to the provider's DB primary key id,
     * since bookings store providerId as the provider DB id.
     */
    private Long resolveProviderDbId(Long userId) {
        ProviderResponse provider = callWithFallback(
                () -> providerServiceClient.getProviderByUserId(userId),
                "Provider profile not found. Please register as a provider first."
        );
        return provider.getId();
    }

    // ===================== TRANSITION VALIDATION =====================

    private void validateTransition(BookingStatus current, BookingStatus next) {
        Set<BookingStatus> allowed = VALID_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(next)) {
            throw new BookingException(
                    String.format("Invalid status transition: %s -> %s is not allowed", current, next)
            );
        }
    }

    private BookingStatus parseStatus(String status) {
        try {
            return BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookingException("Invalid status value: " + status +
                    ". Allowed values: PENDING, ACCEPTED, ON_THE_WAY, STARTED, COMPLETED, CANCELLED");
        }
    }



    // ===================== ENRICHMENT =====================

    private BookingResponse enrichResponse(Booking booking) {
        BookingResponse response = BookingResponse.fromBooking(booking);

        try {
            CustomerResponse customer = customerServiceClient.getCustomerByUserId(booking.getCustomerId());
            enrichCustomerInfo(response, customer);
        } catch (FeignException e) {
            log.warn("Failed to enrich customer info for booking {}: {}", booking.getId(), e.getMessage());
        }

        try {
            ProviderResponse provider = providerServiceClient.getProviderById(booking.getProviderId());
            response.setProviderName(provider.getName());
        } catch (FeignException e) {
            log.warn("Failed to enrich provider info for booking {}: {}", booking.getId(), e.getMessage());
        }

        try {
            ServiceResponse service = serviceCatalogClient.getServiceById(booking.getServiceId());
            response.setServiceName(service.getName());
            response.setServicePrice(service.getBasePrice());
        } catch (FeignException e) {
            log.warn("Failed to enrich service info for booking {}: {}", booking.getId(), e.getMessage());
        }

        return response;
    }

    private BookingResponse enrichResponse(Booking booking, CustomerResponse customer,
                                           ProviderResponse provider, ServiceResponse service) {
        BookingResponse response = BookingResponse.fromBooking(booking);
        enrichCustomerInfo(response, customer);
        response.setProviderName(provider.getName());
        response.setServiceName(service.getName());
        response.setServicePrice(service.getBasePrice());
        return response;
    }

    private void enrichCustomerInfo(BookingResponse response, CustomerResponse customer) {
        response.setCustomerName(customer.getFullName());
        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            CustomerResponse.AddressResponse addr = customer.getAddresses().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                    .findFirst()
                    .orElse(customer.getAddresses().get(0));
            response.setCustomerAddress(
                    addr.getStreet() + ", " + addr.getCity() + ", " + addr.getState() + " " + addr.getZip()
            );
        }
    }
}
