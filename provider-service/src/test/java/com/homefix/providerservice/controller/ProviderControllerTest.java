package com.homefix.providerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homefix.providerservice.dto.AvailabilityUpdateRequest;
import com.homefix.providerservice.dto.ProviderRegistrationRequest;
import com.homefix.providerservice.dto.ProviderResponse;
import com.homefix.providerservice.entity.AvailabilityStatus;
import com.homefix.providerservice.entity.VerificationStatus;
import com.homefix.providerservice.service.ProviderService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProviderControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProviderService providerService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    private UsernamePasswordAuthenticationToken providerAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));
    }

    @Test
    void registerProfile_ShouldReturn201() throws Exception {
        ProviderRegistrationRequest request = ProviderRegistrationRequest.builder()
                .name("Jane Plumber").experienceYears(5).skills(List.of("Pipe Repair")).build();

        ProviderResponse response = ProviderResponse.builder()
                .id(1L).userId(1L).name("Jane Plumber").experienceYears(5)
                .skills(List.of("Pipe Repair")).availability(AvailabilityStatus.AVAILABLE)
                .verificationStatus(VerificationStatus.PENDING).createdAt(LocalDateTime.now())
                .build();

        when(providerService.registerProfile(eq(1L), any(ProviderRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/profile")
                        .with(authentication(providerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane Plumber"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
    }

    @Test
    void registerProfile_ShouldReturn400_WhenInvalid() throws Exception {
        ProviderRegistrationRequest request = ProviderRegistrationRequest.builder()
                .name("J")
                .build();

        mockMvc.perform(post("/profile")
                        .with(authentication(providerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyProfile_ShouldReturn200() throws Exception {
        ProviderResponse response = ProviderResponse.builder()
                .id(1L).userId(1L).name("Jane").experienceYears(5)
                .skills(List.of()).availability(AvailabilityStatus.AVAILABLE)
                .verificationStatus(VerificationStatus.PENDING).createdAt(LocalDateTime.now())
                .build();

        when(providerService.getMyProfile(1L)).thenReturn(response);

        mockMvc.perform(get("/me").with(authentication(providerAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    void updateAvailability_ShouldReturn200() throws Exception {
        AvailabilityUpdateRequest request = AvailabilityUpdateRequest.builder()
                .availability(AvailabilityStatus.BUSY).build();

        ProviderResponse response = ProviderResponse.builder()
                .id(1L).userId(1L).name("Jane").experienceYears(5)
                .skills(List.of()).availability(AvailabilityStatus.BUSY)
                .verificationStatus(VerificationStatus.PENDING).createdAt(LocalDateTime.now())
                .build();

        when(providerService.updateAvailability(eq(1L), any(AvailabilityUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/me/availability")
                        .with(authentication(providerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availability").value("BUSY"));
    }

    @Test
    void getProviderById_ShouldReturn200_WithoutAuth() throws Exception {
        ProviderResponse response = ProviderResponse.builder()
                .id(1L).userId(1L).name("Jane Public").experienceYears(5)
                .skills(List.of("Plumbing")).availability(AvailabilityStatus.AVAILABLE)
                .verificationStatus(VerificationStatus.VERIFIED).createdAt(LocalDateTime.now())
                .build();

        when(providerService.getProviderById(1L)).thenReturn(response);

        mockMvc.perform(get("/profile/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Public"));
    }
}
