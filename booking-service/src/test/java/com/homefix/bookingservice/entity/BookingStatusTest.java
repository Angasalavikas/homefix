package com.homefix.bookingservice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingStatusTest {

    @Test
    void shouldHaveAllRequiredStatusValues() {
        // Payment state is tracked separately via PaymentStatus — it must NOT
        // appear in the lifecycle status enum.
        assertEquals(6, BookingStatus.values().length);
        assertNotNull(BookingStatus.valueOf("PENDING"));
        assertNotNull(BookingStatus.valueOf("ACCEPTED"));
        assertNotNull(BookingStatus.valueOf("ON_THE_WAY"));
        assertNotNull(BookingStatus.valueOf("STARTED"));
        assertNotNull(BookingStatus.valueOf("COMPLETED"));
        assertNotNull(BookingStatus.valueOf("CANCELLED"));
        assertThrows(IllegalArgumentException.class, () -> BookingStatus.valueOf("PAID"));
    }

    @Test
    void paymentStatusIsIndependentOfLifecycleStatus() {
        assertEquals(2, PaymentStatus.values().length);
        assertNotNull(PaymentStatus.valueOf("UNPAID"));
        assertNotNull(PaymentStatus.valueOf("PAID"));
    }
}
