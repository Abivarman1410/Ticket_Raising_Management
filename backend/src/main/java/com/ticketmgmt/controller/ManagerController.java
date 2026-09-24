package com.ticketmgmt.controller;

import com.ticketmgmt.dto.request.*;
import com.ticketmgmt.dto.response.*;
import com.ticketmgmt.entity.*;
import com.ticketmgmt.enums.TicketPriority;
import com.ticketmgmt.enums.TicketStatus;
import com.ticketmgmt.exception.BadRequestException;
import com.ticketmgmt.exception.ResourceNotFoundException;
import com.ticketmgmt.repository.*;
import com.ticketmgmt.service.ManagerService;
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

@Tag(name = "Manager APIs", description = "Organization-wide ticket monitoring and admin management")
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ManagerController {

    private final ManagerService                    managerService;
    private final TicketService                     ticketService;
    private final AdminCategoryAssignmentRepository adminCategoryAssignmentRepository;
    private final UserRepository                    userRepository;
    private final TicketRepository                  ticketRepository;

    // ---- Admin Management ----

    @Operation(summary = "Create an admin account")
    @PostMapping("/admins")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AdminResponse> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managerService.createAdmin(request));
    }

    @Operation(summary = "List all admin accounts")
    @GetMapping("/admins")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        return ResponseEntity.ok(managerService.getAllAdmins());
    }

    @Operation(summary = "Get a specific admin account")
    @GetMapping("/admins/{adminId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AdminResponse> getAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(managerService.getAdminById(adminId));
    }

    @Operation(summary = "Activate or deactivate an admin account")
    @PatchMapping("/admins/{adminId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AdminResponse> updateAdminStatus(
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateAdminStatusRequest request) {
        return ResponseEntity.ok(managerService.updateAdminStatus(adminId, request));
    }

    @Operation(summary = "Assign a category to an admin")
    @PostMapping("/admins/{adminId}/categories")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<AdminResponse> addCategory(
            @PathVariable Long adminId,
            @Valid @RequestBody UpdateAdminCategoryRequest request) {
        return ResponseEntity.ok(managerService.addCategoryToAdmin(adminId, request));
    }

    @Operation(summary = "Remove a category from an admin")
    @DeleteMapping("/admins/{adminId}/categories/{categoryId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeCategory(@PathVariable Long adminId,
                                               @PathVariable Long categoryId) {
        managerService.removeCategoryFromAdmin(adminId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // ---- Ticket Monitoring ----

    @Operation(summary = "View all organization tickets with optional filters")
    @GetMapping("/tickets")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(ticketService.getAllTickets(status, priority, categoryId));
    }

    @Operation(summary = "View a specific ticket")
    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getTicketByIdForManager(ticketId));
    }

    @Operation(summary = "View ticket status history")
    @GetMapping("/tickets/{ticketId}/history")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TicketStatusHistory>> getHistory(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getStatusHistory(ticketId));
    }

    @Operation(summary = "View ticket assignment history")
    @GetMapping("/tickets/{ticketId}/assignment-history")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TicketAssignmentHistory>> getAssignmentHistory(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ticketService.getAssignmentHistory(ticketId));
    }

    @Operation(summary = "Reassign a ticket to a specific admin (manager authorized)")
    @PostMapping("/tickets/{ticketId}/reassign")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TicketResponse> reassignTicket(
            @PathVariable Long ticketId,
            @RequestBody ReassignTicketRequest request) {

        // Fetch the ticket's category from DB
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot reassign a closed ticket.");
        }

        User admin = userRepository.findById(request.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + request.getAdminId()));

        if (!Boolean.TRUE.equals(admin.getIsActive())) {
            throw new BadRequestException("Admin " + admin.getEmployeeId() + " is inactive.");
        }

        if (!"ADMIN".equals(admin.getRole().getName())) {
            throw new BadRequestException("User " + admin.getEmployeeId() + " is not an admin.");
        }

        AdminCategoryAssignment newMapping = adminCategoryAssignmentRepository
                .findByAdminIdAndCategoryIdAndIsActiveTrue(admin.getId(), ticket.getCategory().getId())
                .orElseThrow(() -> new BadRequestException(
                        "Admin " + admin.getEmployeeId() + " is not eligible for category "
                                + ticket.getCategory().getName()));

        return ResponseEntity.ok(ticketService.reassignTicket(ticketId, request, newMapping));
    }

    // ---- Dashboard ----

    @Operation(summary = "Get manager dashboard metrics")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(managerService.getDashboardMetrics());
    }
}
