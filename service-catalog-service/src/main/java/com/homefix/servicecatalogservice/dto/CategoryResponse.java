package com.homefix.servicecatalogservice.dto;

import com.homefix.servicecatalogservice.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private LocalDateTime createdAt;

    public static CategoryResponse fromCategory(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .icon(category.getIcon())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
