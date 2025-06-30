package com.fpt.metroll.order.service.impl;

import com.fpt.metroll.order.domain.dto.OrderDashboardDto;
import com.fpt.metroll.order.repository.OrderDetailRepository;
import com.fpt.metroll.order.repository.OrderRepository;
import com.fpt.metroll.order.service.OrderDashboardService;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.OrderStatus;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class OrderDashboardServiceImpl implements OrderDashboardService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public OrderDashboardServiceImpl(OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    @Override
    public OrderDashboardDto getDashboard() {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        // Get all orders
        var allOrders = orderRepository.findAll();

        // Count orders by status
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            long count = allOrders.stream()
                    .filter(order -> order.getStatus() == status)
                    .count();
            ordersByStatus.put(status.name(), count);
        }

        // Calculate total revenue (only completed orders)
        BigDecimal totalRevenue = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(order -> order.getFinalTotal())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate today's revenue
        Instant startOfToday = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant endOfToday = startOfToday.plus(1, ChronoUnit.DAYS);
        BigDecimal todayRevenue = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> order.getCreatedAt().isAfter(startOfToday) &&
                        order.getCreatedAt().isBefore(endOfToday))
                .map(order -> order.getFinalTotal())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate weekly revenue (last 7 days)
        Instant startOfWeek = LocalDate.now().minusDays(7).atStartOfDay().toInstant(ZoneOffset.UTC);
        BigDecimal weeklyRevenue = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> order.getCreatedAt().isAfter(startOfWeek))
                .map(order -> order.getFinalTotal())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate monthly revenue (last 30 days)
        Instant startOfMonth = LocalDate.now().minusDays(30).atStartOfDay().toInstant(ZoneOffset.UTC);
        BigDecimal monthlyRevenue = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> order.getCreatedAt().isAfter(startOfMonth))
                .map(order -> order.getFinalTotal())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count today's orders
        long todayOrders = allOrders.stream()
                .filter(order -> order.getCreatedAt() != null)
                .filter(order -> order.getCreatedAt().isAfter(startOfToday) &&
                        order.getCreatedAt().isBefore(endOfToday))
                .count();

        // Calculate average order value
        long completedOrdersCount = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();
        double averageOrderValue = completedOrdersCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedOrdersCount), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // Revenue for last 7 days
        Map<String, BigDecimal> revenueLast7Days = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            Instant dayStart = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);

            BigDecimal dayRevenue = allOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                    .filter(order -> order.getCreatedAt() != null)
                    .filter(order -> order.getCreatedAt().isAfter(dayStart) &&
                            order.getCreatedAt().isBefore(dayEnd))
                    .map(order -> order.getFinalTotal())
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenueLast7Days.put(date.toString(), dayRevenue);
        }

        // Get total order details count
        long totalOrderDetails = orderDetailRepository.count();

        return OrderDashboardDto.builder()
                .totalOrders((long) allOrders.size())
                .ordersByStatus(ordersByStatus)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .weeklyRevenue(weeklyRevenue)
                .monthlyRevenue(monthlyRevenue)
                .totalOrderDetails(totalOrderDetails)
                .averageOrderValue(Math.round(averageOrderValue * 100.0) / 100.0)
                .todayOrders(todayOrders)
                .revenueLast7Days(revenueLast7Days)
                .lastUpdated(Instant.now())
                .build();
    }
}