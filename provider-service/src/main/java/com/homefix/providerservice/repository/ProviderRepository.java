package com.homefix.providerservice.repository;

import com.homefix.providerservice.entity.AvailabilityStatus;
import com.homefix.providerservice.entity.Provider;
import com.homefix.providerservice.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Provider> findByVerificationStatus(VerificationStatus verificationStatus);
    List<Provider> findByVerificationStatusAndAvailability(
            VerificationStatus verificationStatus,
            AvailabilityStatus availability);
}
