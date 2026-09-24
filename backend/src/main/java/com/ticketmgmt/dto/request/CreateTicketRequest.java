package com.ticketmgmt.dto.request;

import com.ticketmgmt.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketRequest {

    @NotBlank(message = "Category is required")
    private String category;   // "HARDWARE" or "SOFTWARE"

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Priority is required")
    private TicketPriority priority;

    private String businessUnit;
    private String workLocation;
    private String assetTag;
}
