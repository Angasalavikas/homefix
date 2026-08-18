package com.homefix.servicecatalogservice.service;

import com.homefix.servicecatalogservice.dto.*;
import com.homefix.servicecatalogservice.exception.ResourceNotFoundException;
import com.homefix.servicecatalogservice.entity.Category;
import com.homefix.servicecatalogservice.entity.ServiceItem;
import com.homefix.servicecatalogservice.repository.CategoryRepository;
import com.homefix.servicecatalogservice.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;

    // ==================== Category CRUD ====================

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .build();

        category = categoryRepository.save(category);
        return CategoryResponse.fromCategory(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromCategory)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return CategoryResponse.fromCategory(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIcon() != null) category.setIcon(request.getIcon());

        category = categoryRepository.save(category);
        return CategoryResponse.fromCategory(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    // ==================== Service Item CRUD ====================

    @Transactional
    public ServiceItemResponse createService(ServiceItemRequest request) {
        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new IllegalArgumentException("Category with ID " + request.getCategoryId() + " not found");
        }

        ServiceItem item = ServiceItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .basePrice(request.getBasePrice())
                .durationMinutes(request.getDurationMinutes())
                .build();

        item = serviceItemRepository.save(item);
        return ServiceItemResponse.fromServiceItem(item);
    }

    public List<ServiceItemResponse> getAllServices() {
        return serviceItemRepository.findAll().stream()
                .map(item -> {
                    Category category = categoryRepository.findById(item.getCategoryId()).orElse(null);
                    String categoryName = category != null ? category.getName() : null;
                    return ServiceItemResponse.fromServiceItemWithCategoryName(item, categoryName);
                })
                .collect(Collectors.toList());
    }

    public ServiceItemResponse getServiceById(Long id) {
        ServiceItem item = serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        Category category = categoryRepository.findById(item.getCategoryId()).orElse(null);
        String categoryName = category != null ? category.getName() : null;
        return ServiceItemResponse.fromServiceItemWithCategoryName(item, categoryName);
    }

    @Transactional
    public ServiceItemResponse updateService(Long id, ServiceItemRequest request) {
        ServiceItem item = serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (request.getCategoryId() != null && !categoryRepository.existsById(request.getCategoryId())) {
            throw new IllegalArgumentException("Category with ID " + request.getCategoryId() + " not found");
        }

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategoryId() != null) item.setCategoryId(request.getCategoryId());
        if (request.getBasePrice() != null) item.setBasePrice(request.getBasePrice());
        if (request.getDurationMinutes() != null) item.setDurationMinutes(request.getDurationMinutes());

        item = serviceItemRepository.save(item);
        return ServiceItemResponse.fromServiceItem(item);
    }

    @Transactional
    public void deleteService(Long id) {
        if (!serviceItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service not found");
        }
        serviceItemRepository.deleteById(id);
    }

    // ==================== Public Search ====================

    public List<ServiceItemResponse> searchServices(Long categoryId, String keyword) {
        List<ServiceItem> results = serviceItemRepository.searchServices(categoryId, keyword);
        return results.stream()
                .map(item -> {
                    Category category = categoryRepository.findById(item.getCategoryId()).orElse(null);
                    String categoryName = category != null ? category.getName() : null;
                    return ServiceItemResponse.fromServiceItemWithCategoryName(item, categoryName);
                })
                .collect(Collectors.toList());
    }
}
