package com.homefix.bookingservice.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingStatusTest {

    @Test
    void shouldHaveAllRequiredStatusValues() {
        assertEquals(6, BookingStatus.values().length);
        assertNotNull(BookingStatus.valueOf("PENDING"));
        assertNotNull(BookingStatus.valueOf("ACCEPTED"));
        assertNotNull(BookingStatus.valueOf("ON_THE_WAY"));
        assertNotNull(BookingStatus.valueOf("STARTED"));
        assertNotNull(BookingStatus.valueOf("COMPLETED"));
        assertNotNull(BookingStatus.valueOf("CANCELLED"));
    }
}
