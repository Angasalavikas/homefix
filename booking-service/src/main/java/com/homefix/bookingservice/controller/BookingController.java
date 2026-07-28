package com.homefix.bookingservice.controller;

import com.homefix.bookingservice.dto.BookingRequest;
import com.homefix.bookingservice.dto.BookingResponse;
import com.homefix.bookingservice.dto.StatusUpdateRequest;
import com.homefix.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /bookings — Customer creates a new booking.
     * customerId is resolved from JWT.
     */
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        BookingResponse response = bookingService.createBooking(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /bookings?role=customer|provider&status= — List bookings for the authenticated user.
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> listBookings(
            @RequestParam(defaultValue = "customer") String role,
            @RequestParam(required = false) String status,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<BookingResponse> responses = bookingService.listBookings(userId, role, status);
        return ResponseEntity.ok(responses);
    }

    /**
     * PUT /bookings/{id}/status — Provider or Admin updates booking status.
     * Enforces valid status transition graph.
     * For providers: resolves JWT userId -> provider DB id automatically.
     * For admins: allows status update on any booking.
     */
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        BookingResponse response = bookingService.updateStatus(id, userId, isAdmin, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /bookings/{id}/cancel — Customer cancels a booking.
     * Only allowed from PENDING or ACCEPTED status.
     */
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        Long customerId = (Long) authentication.getPrincipal();
        BookingResponse response = bookingService.cancelBooking(id, customerId);
        return ResponseEntity.ok(response);
    }
}
