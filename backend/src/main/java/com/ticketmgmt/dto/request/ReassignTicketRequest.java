package com.ticketmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReassignTicketRequest {

    @NotBlank(message = "Admin ID is required")
    private Long adminId;

    private String reason;
}
