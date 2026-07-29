package com.homefix.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCustomerResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
