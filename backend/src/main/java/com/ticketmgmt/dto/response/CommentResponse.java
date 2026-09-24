package com.ticketmgmt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long          id;
    private Long          ticketId;
    private Long          userId;
    private String        userName;
    private String        commentText;
    private LocalDateTime createdAt;
}
