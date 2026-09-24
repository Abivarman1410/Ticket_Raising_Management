package com.ticketmgmt.controller;

import com.ticketmgmt.dto.request.CreateTicketRequest;
import com.ticketmgmt.dto.request.TicketConfirmationRequest;
import com.ticketmgmt.dto.response.TicketResponse;
import com.ticketmgmt.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Employee APIs", description = "Ticket creation and management for employees")
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final TicketService ticketService;

    @Operation(summary = "Create a new ticket")
    @PostMapping("/tickets")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }

    @Operation(summary = "View my tickets")
    @GetMapping("/tickets")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TicketResponse>> getMyTickets() {
        return ResponseEntity.ok(ticketService.getMyTickets());
    }

    @Operation(summary = "View a specific ticket")
    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getMyTicketById(ticketId));
    }

    @Operation(summary = "Confirm or reject a resolution")
    @PostMapping("/tickets/{ticketId}/confirmation")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TicketResponse> confirmResolution(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketConfirmationRequest request) {
        return ResponseEntity.ok(ticketService.confirmResolution(ticketId, request));
    }
}
