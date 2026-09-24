package com.ticketmgmt.controller;

import com.ticketmgmt.dto.request.AddCommentRequest;
import com.ticketmgmt.dto.request.SubmitResolutionRequest;
import com.ticketmgmt.dto.response.CommentResponse;
import com.ticketmgmt.dto.response.TicketResponse;

import com.ticketmgmt.entity.TicketStatusHistory;
import com.ticketmgmt.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin APIs", description = "Ticket management for admins")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final TicketService ticketService;

    @Operation(summary = "View all assigned tickets")
    @GetMapping("/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> getAssignedTickets() {
        return ResponseEntity.ok(ticketService.getAssignedTickets());
    }

    @Operation(summary = "View a specific assigned ticket")
    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getAssignedTicketById(ticketId));
    }

    @Operation(summary = "Start working on a ticket (ASSIGNED/REOPENED → IN_PROGRESS)")
    @PatchMapping("/tickets/{ticketId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> startWorking(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.updateTicketStatusByAdmin(ticketId));
    }

    @Operation(summary = "Submit resolution for a ticket")
    @PostMapping("/tickets/{ticketId}/resolution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> submitResolution(
            @PathVariable Long ticketId,
            @Valid @RequestBody SubmitResolutionRequest request) {
        return ResponseEntity.ok(ticketService.submitResolution(ticketId, request));
    }

    @Operation(summary = "Add a comment to a ticket")
    @PostMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentRequest request) {
        return ResponseEntity.ok(ticketService.addComment(ticketId, request));
    }

    @Operation(summary = "View ticket status history")
    @GetMapping("/tickets/{ticketId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketStatusHistory>> getStatusHistory(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getStatusHistory(ticketId));
    }

    @Operation(summary = "View ticket comments")
    @GetMapping("/tickets/{ticketId}/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getComments(ticketId));
    }
}
