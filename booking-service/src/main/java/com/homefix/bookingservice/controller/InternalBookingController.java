package com.homefix.bookingservice.controller;

import com.homefix.bookingservice.dto.BookingResponse;
import com.homefix.bookingservice.dto.InternalPaymentStatusUpdateRequest;
import com.homefix.bookingservice.entity.Booking;
import com.homefix.bookingservice.entity.BookingStatus;
import com.homefix.bookingservice.repository.BookingRepository;
import com.homefix.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalBookingController {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /**
     * GET /internal/bookings — List all bookings for admin.
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.listAllBookings());
    }

    /**
     * GET /internal/bookings/{id} — Fetch a single booking by id (used by payment-service).
     * Returns 404 so payment-service can report "Booking not found" accurately.
     */
    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(booking -> ResponseEntity.ok(BookingResponse.fromBooking(booking)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /internal/bookings/{id}/payment-status — Internal update used by
     * payment-service after a successful payment. Updates ONLY paymentStatus
     * (UNPAID/PAID); the booking's lifecycle status is never changed here.
     */
    @PutMapping("/bookings/{id}/payment-status")
    public ResponseEntity<BookingResponse> updateBookingPaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody InternalPaymentStatusUpdateRequest request) {
        BookingResponse response = bookingService.internalUpdatePaymentStatus(id, request.getPaymentStatus());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /internal/bookings/stats — Bookings count grouped by status.
     * Uses a single efficient query.
     */
    @GetMapping("/bookings/stats")
    public ResponseEntity<Map<String, Long>> getBookingStats() {
        List<Object[]> results = bookingRepository.countByStatus();
        Map<String, Long> stats = new LinkedHashMap<>();
        // Initialize all statuses with 0
        for (BookingStatus status : BookingStatus.values()) {
            stats.put(status.name(), 0L);
        }
        // Fill in actual counts
        for (Object[] row : results) {
            stats.put(((BookingStatus) row[0]).name(), (Long) row[1]);
        }
        return ResponseEntity.ok(stats);
    }
}
