package com.homefix.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequest {

    @NotBlank(message = "Label is required")
    @Size(max = 50, message = "Label must be at most 50 characters")
    private String label;

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must be at most 255 characters")
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must be at most 50 characters")
    private String state;

    @NotBlank(message = "ZIP code is required")
    @Size(max = 20, message = "ZIP code must be at most 20 characters")
    private String zip;

    private Boolean isDefault;
}
