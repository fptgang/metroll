package com.fpt.metroll.order.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDashboardDto {
    private Long totalOrders;
    private Map<String, Long> ordersByStatus;
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weeklyRevenue;
    private BigDecimal monthlyRevenue;
    private Long totalOrderDetails;
    private Double averageOrderValue;
    private Long todayOrders;
    private Map<String, BigDecimal> revenueLast7Days;
    private Instant lastUpdated;
}