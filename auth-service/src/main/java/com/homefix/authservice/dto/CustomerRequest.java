package com.homefix.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CustomerRequest {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
}

