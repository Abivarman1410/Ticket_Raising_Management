package com.ticketmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAdminCategoryRequest {

    @NotBlank(message = "Category is required")
    private String category;  // HARDWARE or SOFTWARE

    private Boolean isActive = true;
}
