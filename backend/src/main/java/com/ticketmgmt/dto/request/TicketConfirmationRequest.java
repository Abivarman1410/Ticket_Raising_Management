package com.ticketmgmt.dto.request;

import com.ticketmgmt.enums.ConfirmationDecision;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketConfirmationRequest {

    @NotNull(message = "Decision is required")
    private ConfirmationDecision decision;

    private String reason;  // Required when REJECTED, optional when CONFIRMED
}
