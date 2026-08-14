package com.homefix.paymentservice.repository;

import com.homefix.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}
