package com.homefix.customerservice.dto;

import com.homefix.customerservice.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private List<AddressResponse> addresses;

    public static CustomerResponse fromCustomer(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUserId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .createdAt(customer.getCreatedAt())
                .addresses(customer.getAddresses().stream()
                        .map(AddressResponse::fromAddress)
                        .collect(Collectors.toList()))
                .build();
    }
}
