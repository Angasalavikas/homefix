package com.homefix.bookingservice.controller;

import com.homefix.bookingservice.dto.BookingResponse;
import com.homefix.bookingservice.entity.Booking;
import com.homefix.bookingservice.entity.BookingStatus;
import com.homefix.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalBookingController {

    private final BookingRepository bookingRepository;

    /**
     * GET /internal/bookings — List all bookings for admin.
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingRepository.findAll().stream()
                .map(BookingResponse::fromBooking)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
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
