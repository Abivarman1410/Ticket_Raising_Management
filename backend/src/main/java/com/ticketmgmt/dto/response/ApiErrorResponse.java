package com.ticketmgmt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int           status;
    private String        error;
    private String        message;
    private String        path;
}
