package com.fpt.metroll.shared.domain.enums;

public enum TicketStatus {
    VALID, // Ticket is valid and can be used
    USED, // Ticket has been used (for P2P tickets)
    EXPIRED, // Ticket has expired
    CANCELLED // Ticket has been cancelled
}