package com.ticketmgmt.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private Long   userId;
    private String employeeId;
    private String fullName;
    private String email;
    private String role;
}
