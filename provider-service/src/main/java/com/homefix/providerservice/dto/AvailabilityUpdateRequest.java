package com.homefix.providerservice.dto;

import com.homefix.providerservice.entity.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityUpdateRequest {

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus availability;
}
