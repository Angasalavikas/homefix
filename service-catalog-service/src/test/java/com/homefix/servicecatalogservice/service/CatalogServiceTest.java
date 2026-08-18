package com.homefix.servicecatalogservice.service;

import com.homefix.servicecatalogservice.dto.*;
import com.homefix.servicecatalogservice.entity.Category;
import com.homefix.servicecatalogservice.entity.ServiceItem;
import com.homefix.servicecatalogservice.repository.CategoryRepository;
import com.homefix.servicecatalogservice.repository.ServiceItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ServiceItemRepository serviceItemRepository;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(categoryRepository, serviceItemRepository);
    }

    // ==================== Category Tests ====================

    @Test
    void createCategory_ShouldCreateNewCategory() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Plumbing")
                .description("Plumbing services")
                .icon("wrench")
                .build();

        when(categoryRepository.existsByName("Plumbing")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(java.time.LocalDateTime.now());
            saved.setUpdatedAt(java.time.LocalDateTime.now());
            return saved;
        });

        CategoryResponse response = catalogService.createCategory(request);

        assertThat(response.getName()).isEqualTo("Plumbing");
        assertThat(response.getDescription()).isEqualTo("Plumbing services");
        assertThat(response.getIcon()).isEqualTo("wrench");
    }

    @Test
    void createCategory_ShouldThrowException_WhenDuplicateName() {
        when(categoryRepository.existsByName("Plumbing")).thenReturn(true);

        CategoryRequest request = CategoryRequest.builder().name("Plumbing").build();

        assertThatThrownBy(() -> catalogService.createCategory(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getAllCategories_ShouldReturnAll() {
        Category cat1 = createCategory(1L, "Plumbing");
        Category cat2 = createCategory(2L, "Electrical");

        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        List<CategoryResponse> responses = catalogService.getAllCategories();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Plumbing");
        assertThat(responses.get(1).getName()).isEqualTo("Electrical");
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenFound() {
        Category category = createCategory(1L, "Plumbing");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponse response = catalogService.getCategoryById(1L);

        assertThat(response.getName()).isEqualTo("Plumbing");
    }

    @Test
    void getCategoryById_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getCategoryById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateCategory_ShouldUpdateFields() {
        Category category = createCategory(1L, "Plumbing");

        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Plumbing")
                .description("Updated description")
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = catalogService.updateCategory(1L, request);

        assertThat(response.getName()).isEqualTo("Updated Plumbing");
        assertThat(response.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void deleteCategory_ShouldDelete_WhenExists() {
        when(categoryRepository.existsById(1L)).thenReturn(true);

        catalogService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_ShouldThrowException_WhenNotFound() {
        when(categoryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> catalogService.deleteCategory(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // ==================== Service Tests ====================

    @Test
    void createService_ShouldCreateNewService() {
        ServiceItemRequest request = ServiceItemRequest.builder()
                .name("Faucet Repair")
                .description("Fix leaking faucets")
                .categoryId(1L)
                .basePrice(new BigDecimal("80.00"))
                .durationMinutes(60)
                .build();

        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(serviceItemRepository.save(any(ServiceItem.class))).thenAnswer(invocation -> {
            ServiceItem saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setCreatedAt(java.time.LocalDateTime.now());
            saved.setUpdatedAt(java.time.LocalDateTime.now());
            return saved;
        });

        ServiceItemResponse response = catalogService.createService(request);

        assertThat(response.getName()).isEqualTo("Faucet Repair");
        assertThat(response.getBasePrice()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(response.getDurationMinutes()).isEqualTo(60);
    }

    @Test
    void createService_ShouldThrowException_WhenCategoryNotFound() {
        ServiceItemRequest request = ServiceItemRequest.builder()
                .name("Test Service")
                .categoryId(999L)
                .basePrice(new BigDecimal("50.00"))
                .durationMinutes(30)
                .build();

        when(categoryRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> catalogService.createService(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getAllServices_ShouldReturnAllWithCategoryNames() {
        Category category = createCategory(1L, "Plumbing");
        ServiceItem item1 = createServiceItem(100L, "Faucet Repair", 1L);
        ServiceItem item2 = createServiceItem(101L, "Drain Cleaning", 1L);

        when(serviceItemRepository.findAll()).thenReturn(List.of(item1, item2));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        List<ServiceItemResponse> responses = catalogService.getAllServices();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCategoryName()).isEqualTo("Plumbing");
    }

    @Test
    void getServiceById_ShouldReturnService_WhenFound() {
        Category category = createCategory(1L, "Plumbing");
        ServiceItem item = createServiceItem(100L, "Faucet Repair", 1L);

        when(serviceItemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        ServiceItemResponse response = catalogService.getServiceById(100L);

        assertThat(response.getName()).isEqualTo("Faucet Repair");
        assertThat(response.getCategoryName()).isEqualTo("Plumbing");
    }

    @Test
    void getServiceById_ShouldThrowException_WhenNotFound() {
        when(serviceItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getServiceById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateService_ShouldUpdateFields() {
        ServiceItem item = createServiceItem(100L, "Faucet Repair", 1L);

        ServiceItemRequest request = ServiceItemRequest.builder()
                .name("Premium Faucet Repair")
                .basePrice(new BigDecimal("120.00"))
                .durationMinutes(90)
                .build();

        when(serviceItemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(serviceItemRepository.save(any(ServiceItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceItemResponse response = catalogService.updateService(100L, request);

        assertThat(response.getName()).isEqualTo("Premium Faucet Repair");
        assertThat(response.getBasePrice()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(response.getDurationMinutes()).isEqualTo(90);
    }

    @Test
    void deleteService_ShouldDelete_WhenExists() {
        when(serviceItemRepository.existsById(100L)).thenReturn(true);

        catalogService.deleteService(100L);

        verify(serviceItemRepository).deleteById(100L);
    }

    @Test
    void deleteService_ShouldThrowException_WhenNotFound() {
        when(serviceItemRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> catalogService.deleteService(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void searchServices_ShouldReturnFilteredResults() {
        Category category = createCategory(1L, "Plumbing");
        ServiceItem item = createServiceItem(100L, "Drain Cleaning", 1L);

        when(serviceItemRepository.searchServices(1L, "drain")).thenReturn(List.of(item));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        List<ServiceItemResponse> results = catalogService.searchServices(1L, "drain");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Drain Cleaning");
    }

    @Test
    void searchServices_ShouldReturnAll_WhenNoFilters() {
        ServiceItem item1 = createServiceItem(100L, "Faucet Repair", 1L);
        ServiceItem item2 = createServiceItem(101L, "Wiring", 2L);

        when(serviceItemRepository.searchServices(null, null)).thenReturn(List.of(item1, item2));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory(1L, "Plumbing")));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(createCategory(2L, "Electrical")));

        List<ServiceItemResponse> results = catalogService.searchServices(null, null);

        assertThat(results).hasSize(2);
    }

    // ==================== Helpers ====================

    private Category createCategory(Long id, String name) {
        Category cat = Category.builder()
                .id(id)
                .name(name)
                .description(name + " services")
                .build();
        cat.setCreatedAt(java.time.LocalDateTime.now());
        cat.setUpdatedAt(java.time.LocalDateTime.now());
        return cat;
    }

    private ServiceItem createServiceItem(Long id, String name, Long categoryId) {
        ServiceItem item = ServiceItem.builder()
                .id(id)
                .name(name)
                .description(name + " description")
                .categoryId(categoryId)
                .basePrice(new BigDecimal("80.00"))
                .durationMinutes(60)
                .build();
        item.setCreatedAt(java.time.LocalDateTime.now());
        item.setUpdatedAt(java.time.LocalDateTime.now());
        return item;
    }
}
