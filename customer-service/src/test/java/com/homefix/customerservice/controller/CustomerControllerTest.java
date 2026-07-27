package com.homefix.customerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homefix.customerservice.dto.AddressRequest;
import com.homefix.customerservice.dto.AddressResponse;
import com.homefix.customerservice.dto.CustomerRequest;
import com.homefix.customerservice.dto.CustomerResponse;
import com.homefix.customerservice.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CustomerControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    private UsernamePasswordAuthenticationToken customerAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void getMyProfile_ShouldReturn200_WhenAuthenticated() throws Exception {
        CustomerResponse response = CustomerResponse.builder()
                .id(1L).userId(1L).fullName("John").email("john@test.com")
                .phone("+12025551234").createdAt(LocalDateTime.now()).addresses(List.of())
                .build();

        when(customerService.getProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/me").with(authentication(customerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John"));
    }

    @Test
    void updateMyProfile_ShouldReturn200() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .fullName("John Updated").email("john@test.com").phone("+12025551234").build();

        CustomerResponse response = CustomerResponse.builder()
                .id(1L).userId(1L).fullName("John Updated").email("john@test.com")
                .phone("+12025551234").createdAt(LocalDateTime.now()).addresses(List.of())
                .build();

        when(customerService.createOrUpdateProfile(eq(1L), any(CustomerRequest.class))).thenReturn(response);

        mockMvc.perform(put("/me")
                        .with(authentication(customerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Updated"));
    }

    @Test
    void addAddress_ShouldReturn201() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .label("Home").street("123 Main").city("NYC").state("NY").zip("10001").isDefault(true)
                .build();

        AddressResponse response = AddressResponse.builder()
                .id(10L).label("Home").street("123 Main").city("NYC").state("NY").zip("10001").isDefault(true)
                .build();

        when(customerService.addAddress(eq(1L), any(AddressRequest.class))).thenReturn(response);

        mockMvc.perform(post("/me/addresses")
                        .with(authentication(customerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Home"));
    }

    @Test
    void updateAddress_ShouldReturn200() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .label("Work").street("123 Main").city("NYC").state("NY").zip("10001")
                .build();
        AddressResponse response = AddressResponse.builder()
                .id(10L).label("Work").street("123 Main").city("NYC").state("NY").zip("10001")
                .build();

        when(customerService.updateAddress(eq(1L), eq(10L), any(AddressRequest.class))).thenReturn(response);

        mockMvc.perform(put("/me/addresses/10")
                        .with(authentication(customerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Work"));
    }

    @Test
    void deleteAddress_ShouldReturn204() throws Exception {
        doNothing().when(customerService).deleteAddress(1L, 10L);

        mockMvc.perform(delete("/me/addresses/10")
                        .with(authentication(customerAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMyAddresses_ShouldReturn200() throws Exception {
        AddressResponse addr = AddressResponse.builder().id(10L).label("Home").street("123 Main").city("NYC").state("NY").zip("10001").build();
        when(customerService.getAddresses(1L)).thenReturn(List.of(addr));

        mockMvc.perform(get("/me/addresses")
                        .with(authentication(customerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Home"));
    }

    @Test
    void updateProfile_ShouldReturn400_WhenInvalid() throws Exception {
        CustomerRequest request = CustomerRequest.builder()
                .fullName("A")
                .email("invalid-email")
                .build();

        mockMvc.perform(put("/me")
                        .with(authentication(customerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
