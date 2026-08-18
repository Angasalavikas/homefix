package com.homefix.bookingservice.entity;

/**
 * Payment state of a booking — completely independent from the booking's
 * lifecycle status. A booking can be PENDING + UNPAID, PENDING + PAID,
 * ACCEPTED + PAID, etc. Only payment-service may flip this to PAID.
 */
public enum PaymentStatus {
    UNPAID,
    PAID
}
