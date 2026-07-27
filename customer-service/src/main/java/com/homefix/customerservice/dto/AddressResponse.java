package com.homefix.customerservice.dto;

import com.homefix.customerservice.entity.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {

    private Long id;
    private String label;
    private String street;
    private String city;
    private String state;
    private String zip;
    private Boolean isDefault;

    public static AddressResponse fromAddress(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zip(address.getZip())
                .isDefault(address.getIsDefault())
                .build();
    }
}
