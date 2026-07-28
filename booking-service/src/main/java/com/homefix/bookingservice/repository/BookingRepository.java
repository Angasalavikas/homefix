package com.homefix.bookingservice.repository;

import com.homefix.bookingservice.entity.Booking;
import com.homefix.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByProviderIdOrderByCreatedAtDesc(Long providerId);

    List<Booking> findByCustomerIdAndStatusOrderByCreatedAtDesc(Long customerId, BookingStatus status);

    List<Booking> findByProviderIdAndStatusOrderByCreatedAtDesc(Long providerId, BookingStatus status);

    Optional<Booking> findByIdAndCustomerId(Long id, Long customerId);

    Optional<Booking> findByIdAndProviderId(Long id, Long providerId);
}
