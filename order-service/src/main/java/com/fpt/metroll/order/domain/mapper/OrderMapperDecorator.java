package com.fpt.metroll.order.domain.mapper;

import com.fpt.metroll.order.document.Order;
import com.fpt.metroll.shared.domain.client.AccountClient;
import com.fpt.metroll.shared.domain.dto.order.OrderDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public abstract class OrderMapperDecorator implements OrderMapper {
    
    @Autowired
    private OrderMapper delegate;
    
    @Autowired
    private AccountClient accountClient;

    @Override
    public OrderDto toDto(Order order) {
        OrderDto dto = delegate.toDto(order);
        
        // Populate the staff field using AccountClient
        if (order.getStaffId() != null) {
            try {
                SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                    dto.setStaff(accountClient.getAccountBasic(order.getStaffId()));
                });
            } catch (Exception e) {
                // Log error but don't fail the mapping
                log.warn("Failed to fetch staff account for ID: {}", order.getStaffId(), e);
                dto.setStaff(null);
            }
        }
        
        // Populate the customer field using AccountClient
        if (order.getCustomerId() != null) {
            try {
                SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                    dto.setCustomer(accountClient.getAccountBasic(order.getCustomerId()));
                });
            } catch (Exception e) {
                // Log error but don't fail the mapping
                log.warn("Failed to fetch customer account for ID: {}", order.getCustomerId(), e);
                dto.setCustomer(null);
            }
        }
        
        return dto;
    }
} 