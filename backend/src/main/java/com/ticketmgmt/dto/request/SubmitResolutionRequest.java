package com.ticketmgmt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitResolutionRequest {

    @NotBlank(message = "Solution description is required")
    private String solutionDescription;
}
