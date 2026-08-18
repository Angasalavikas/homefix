package com.homefix.servicecatalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homefix.servicecatalogservice.dto.ServiceItemRequest;
import com.homefix.servicecatalogservice.dto.ServiceItemResponse;
import com.homefix.servicecatalogservice.service.CatalogService;
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

import java.math.BigDecimal;
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
class ServiceControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @MockitoBean
    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    private UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                2L, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private UsernamePasswordAuthenticationToken customerAuth() {
        return new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void searchServices_ShouldReturn200_Public() throws Exception {
        ServiceItemResponse svc = ServiceItemResponse.builder()
                .id(1L).name("Faucet Repair").categoryId(1L).categoryName("Plumbing")
                .basePrice(new BigDecimal("80.00")).durationMinutes(60)
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.searchServices(null, "faucet")).thenReturn(List.of(svc));

        mockMvc.perform(get("/services/search").param("keyword", "faucet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Faucet Repair"));
    }

    @Test
    void searchServices_ShouldFilterByCategory() throws Exception {
        ServiceItemResponse svc = ServiceItemResponse.builder()
                .id(1L).name("Faucet Repair").categoryId(1L).categoryName("Plumbing")
                .basePrice(new BigDecimal("80.00")).durationMinutes(60)
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.searchServices(1L, null)).thenReturn(List.of(svc));

        mockMvc.perform(get("/services/search").param("category", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Faucet Repair"));
    }

    @Test
    void getAllServices_ShouldReturn200_Public() throws Exception {
        ServiceItemResponse svc = ServiceItemResponse.builder()
                .id(1L).name("Faucet Repair").categoryId(1L)
                .basePrice(new BigDecimal("80.00")).durationMinutes(60)
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.getAllServices()).thenReturn(List.of(svc));

        mockMvc.perform(get("/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Faucet Repair"));
    }

    @Test
    void getServiceById_ShouldReturn200_Public() throws Exception {
        ServiceItemResponse svc = ServiceItemResponse.builder()
                .id(1L).name("Faucet Repair").categoryId(1L)
                .basePrice(new BigDecimal("80.00")).durationMinutes(60)
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.getServiceById(1L)).thenReturn(svc);

        mockMvc.perform(get("/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Faucet Repair"));
    }

    @Test
    void createService_ShouldReturn201_WhenAdmin() throws Exception {
        ServiceItemRequest request = ServiceItemRequest.builder()
                .name("Faucet Repair").description("Fix faucets").categoryId(1L)
                .basePrice(new BigDecimal("80.00")).durationMinutes(60).build();

        ServiceItemResponse response = ServiceItemResponse.builder()
                .id(1L).name("Faucet Repair").categoryId(1L)
                .basePrice(new BigDecimal("80.00")).durationMinutes(60)
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.createService(any(ServiceItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/services")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Faucet Repair"));
    }

    @Test
    void createService_ShouldReturn403_WhenCustomer() throws Exception {
        ServiceItemRequest request = ServiceItemRequest.builder()
                .name("Faucet Repair").categoryId(1L)
                .basePrice(new BigDecimal("80.00")).durationMinutes(60).build();

        mockMvc.perform(post("/services")
                        .with(authentication(customerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateService_ShouldReturn200_WhenAdmin() throws Exception {
        ServiceItemRequest request = ServiceItemRequest.builder()
                .name("Updated Service").categoryId(1L)
                .basePrice(new BigDecimal("100.00")).durationMinutes(90).build();

        ServiceItemResponse response = ServiceItemResponse.builder()
                .id(1L).name("Updated Service").categoryId(1L)
                .basePrice(new BigDecimal("100.00")).durationMinutes(90)
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.updateService(eq(1L), any(ServiceItemRequest.class))).thenReturn(response);

        mockMvc.perform(put("/services/1")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Service"));
    }

    @Test
    void deleteService_ShouldReturn204_WhenAdmin() throws Exception {
        doNothing().when(catalogService).deleteService(1L);

        mockMvc.perform(delete("/services/1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteService_ShouldReturn403_WhenCustomer() throws Exception {
        mockMvc.perform(delete("/services/1")
                        .with(authentication(customerAuth())))
                .andExpect(status().isForbidden());
    }
}
