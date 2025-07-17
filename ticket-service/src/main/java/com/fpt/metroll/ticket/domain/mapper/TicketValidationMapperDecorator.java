package com.fpt.metroll.ticket.domain.mapper;

import com.fpt.metroll.ticket.document.TicketValidation;
import com.fpt.metroll.shared.domain.client.AccountClient;
import com.fpt.metroll.shared.domain.dto.ticket.TicketValidationDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public abstract class TicketValidationMapperDecorator implements TicketValidationMapper {
    
    @Autowired
    private TicketValidationMapper delegate;
    
    @Autowired
    private AccountClient accountClient;

    @Override
    public TicketValidationDto toDto(TicketValidation document) {
        TicketValidationDto dto = delegate.toDto(document);
        
        // Populate the validator field using AccountClient
        if (document.getValidatorId() != null) {
            try {
                SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                    dto.setValidator(accountClient.getAccountBasic(document.getValidatorId()));
                });
            } catch (Exception e) {
                // Log error but don't fail the mapping
                log.warn("Failed to fetch validator account for ID: {}", document.getValidatorId(), e);
                dto.setValidator(null);
            }
        }
        
        return dto;
    }
} 