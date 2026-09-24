package com.ticketmgmt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardResponse {
    private long totalTickets;
    private long openTickets;
    private long assignedTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long reopenedTickets;

    // Breakdowns
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByCategory;
    private Map<String, Long> adminWorkload;     // adminName -> activeTicketCount
    private long unassignedTickets;               // OPEN with no admin
}
