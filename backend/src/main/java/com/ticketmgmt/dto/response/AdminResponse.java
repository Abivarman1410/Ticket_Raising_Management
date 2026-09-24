package com.ticketmgmt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminResponse {
    private Long          id;
    private String        employeeId;
    private String        fullName;
    private String        email;
    private String        contactNumber;
    private Boolean       isActive;
    private List<String>  categories;
    private LocalDateTime createdAt;
}
