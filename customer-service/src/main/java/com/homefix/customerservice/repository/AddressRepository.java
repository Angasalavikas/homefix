package com.homefix.customerservice.repository;

import com.homefix.customerservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerId(Long customerId);
}
