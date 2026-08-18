package com.homefix.bookingservice.feign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<AddressResponse> addresses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressResponse {
        private Long id;
        private String label;
        private String street;
        private String city;
        private String state;
        private String zip;
        private Boolean isDefault;
    }
}
