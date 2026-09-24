package com.ticketmgmt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResolutionResponse {
    private Long          id;
    private Long          ticketId;
    private Long          resolvedById;
    private String        resolvedByName;
    private String        solutionDescription;
    private String        resolutionCode;
    private LocalDateTime resolvedAt;
}
