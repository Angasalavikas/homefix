package com.homefix.servicecatalogservice.dto;

import com.homefix.servicecatalogservice.entity.ServiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceItemResponse {

    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private LocalDateTime createdAt;

    public static ServiceItemResponse fromServiceItem(ServiceItem item) {
        return ServiceItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .categoryId(item.getCategoryId())
                .basePrice(item.getBasePrice())
                .durationMinutes(item.getDurationMinutes())
                .createdAt(item.getCreatedAt())
                .build();
    }

    public static ServiceItemResponse fromServiceItemWithCategoryName(ServiceItem item, String categoryName) {
        return ServiceItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .categoryId(item.getCategoryId())
                .categoryName(categoryName)
                .basePrice(item.getBasePrice())
                .durationMinutes(item.getDurationMinutes())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
