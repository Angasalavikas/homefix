package com.homefix.servicecatalogservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homefix.servicecatalogservice.dto.CategoryRequest;
import com.homefix.servicecatalogservice.dto.CategoryResponse;
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
class CategoryControllerTest {

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
    void getAllCategories_ShouldReturn200_Public() throws Exception {
        CategoryResponse cat = CategoryResponse.builder()
                .id(1L).name("Plumbing").description("Plumbing services")
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.getAllCategories()).thenReturn(List.of(cat));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Plumbing"));
    }

    @Test
    void getCategoryById_ShouldReturn200_Public() throws Exception {
        CategoryResponse cat = CategoryResponse.builder()
                .id(1L).name("Plumbing").description("Plumbing services")
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.getCategoryById(1L)).thenReturn(cat);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Plumbing"));
    }

    @Test
    void createCategory_ShouldReturn201_WhenAdmin() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Plumbing").description("Plumbing services").icon("wrench").build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1L).name("Plumbing").description("Plumbing services").icon("wrench")
                .createdAt(LocalDateTime.now()).build();

        when(catalogService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/categories")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Plumbing"));
    }

    @Test
    void createCategory_ShouldReturn403_WhenCustomer() throws Exception {
        CategoryRequest request = CategoryRequest.builder().name("Plumbing").build();

        mockMvc.perform(post("/categories")
                        .with(authentication(customerAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCategory_ShouldReturn200_WhenAdmin() throws Exception {
        CategoryRequest request = CategoryRequest.builder().name("Updated Plumbing").build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1L).name("Updated Plumbing").createdAt(LocalDateTime.now()).build();

        when(catalogService.updateCategory(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/categories/1")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Plumbing"));
    }

    @Test
    void deleteCategory_ShouldReturn204_WhenAdmin() throws Exception {
        doNothing().when(catalogService).deleteCategory(1L);

        mockMvc.perform(delete("/categories/1")
                        .with(authentication(adminAuth())))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCategory_ShouldReturn403_WhenCustomer() throws Exception {
        mockMvc.perform(delete("/categories/1")
                        .with(authentication(customerAuth())))
                .andExpect(status().isForbidden());
    }
}
