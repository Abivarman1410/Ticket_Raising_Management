package com.ticketmgmt.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAdminRequest {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String contactNumber;

    /**
     * Initial category to assign: HARDWARE or SOFTWARE
     */
    @NotBlank(message = "Category is required")
    private String category;
}
