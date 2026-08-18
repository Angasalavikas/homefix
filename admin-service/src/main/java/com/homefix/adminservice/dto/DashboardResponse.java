package com.homefix.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalCustomers;
    private long totalProviders;
    private Map<String, Long> bookingsByStatus;
    private BigDecimal totalRevenue;
}
