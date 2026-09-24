package com.ticketmgmt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAdminStatusRequest {

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
