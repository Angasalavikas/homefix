package com.homefix.paymentservice.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProviderResponse {

    private Long id;
    private Long userId;
    private String name;
    private Integer experienceYears;
    private String availability;
    private String verificationStatus;
}
