package com.homefix.providerservice.dto;

import com.homefix.providerservice.entity.AvailabilityStatus;
import com.homefix.providerservice.entity.Provider;
import com.homefix.providerservice.entity.VerificationStatus;
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
public class ProviderResponse {

    private Long id;
    private Long userId;
    private String name;
    private Integer experienceYears;
    private List<String> skills;
    private AvailabilityStatus availability;
    private VerificationStatus verificationStatus;
    private LocalDateTime createdAt;

    public static ProviderResponse fromProvider(Provider provider) {
        return ProviderResponse.builder()
                .id(provider.getId())
                .userId(provider.getUserId())
                .name(provider.getName())
                .experienceYears(provider.getExperienceYears())
                .skills(provider.getSkills().stream()
                        .map(ps -> ps.getSkillName())
                        .collect(Collectors.toList()))
                .availability(provider.getAvailability())
                .verificationStatus(provider.getVerificationStatus())
                .createdAt(provider.getCreatedAt())
                .build();
    }
}
