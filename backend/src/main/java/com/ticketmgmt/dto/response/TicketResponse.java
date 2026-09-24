package com.ticketmgmt.dto.response;

import com.ticketmgmt.enums.TicketPriority;
import com.ticketmgmt.enums.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private Long          id;
    private String        ticketNumber;
    private Long          employeeId;
    private String        employeeName;
    private String        employeeEmpId;
    private String        employeeEmail;
    private String        employeeContactNumber;
    private String        category;
    private String        title;
    private String        description;
    private TicketPriority priority;
    private TicketStatus  status;
    private String        assignmentFailureReason;
    
    private String        businessUnit;
    private String        workLocation;
    private String        assetTag;

    // Assigned admin details (exposed to employee limited, full to manager)
    private Long          assignedAdminId;
    private String        assignedAdminName;
    private String        assignedAdminEmpId;

    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime updatedAt;

    // Latest resolution (if any)
    private ResolutionResponse latestResolution;
    private String latestRejectionReason;
}
