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
public class AdminProviderResponse {

    private Long id;
    private Long userId;
    private String name;
    private Integer experienceYears;
    private List<String> skills;
    private String availability;
    private String verificationStatus;
    private LocalDateTime createdAt;
}
