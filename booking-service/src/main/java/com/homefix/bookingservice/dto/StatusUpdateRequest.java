package com.homefix.bookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "(?i)PENDING|ACCEPTED|ON_THE_WAY|STARTED|COMPLETED|CANCELLED",
            message = "Status must be one of: PENDING, ACCEPTED, ON_THE_WAY, STARTED, COMPLETED, CANCELLED")
    private String status;
}
