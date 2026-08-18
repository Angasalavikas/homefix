package com.homefix.bookingservice.service;

import com.homefix.bookingservice.exception.BookingException;
import feign.FeignException;

/**
 * Utility for Feign calls with fallback error handling.
 */
public final class FeignUtils {

    @FunctionalInterface
    public interface FeignCall<T> {
        T call();
    }

    /**
     * Execute a Feign call and wrap common failures into BookingException.
     * 404 (NotFound) gets a clear "resource not found" message.
     * Other errors get a "service unavailable" message.
     */
    public static <T> T callWithFallback(FeignCall<T> call, String errorMessage) {
        try {
            return call.call();
        } catch (FeignException.NotFound e) {
            throw new BookingException(errorMessage);
        } catch (FeignException e) {
            throw new BookingException("Service temporarily unavailable: " + errorMessage);
        }
    }

    private FeignUtils() {}
}
