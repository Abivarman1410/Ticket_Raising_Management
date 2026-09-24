package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.*;
import com.ticketmgmt.dto.response.*;
import com.ticketmgmt.entity.*;
import com.ticketmgmt.enums.*;
import com.ticketmgmt.exception.*;
import com.ticketmgmt.repository.*;
import com.ticketmgmt.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository               ticketRepository;
    private final UserRepository                 userRepository;
    private final IssueCategoryRepository        issueCategoryRepository;
    private final TicketStatusHistoryRepository  ticketStatusHistoryRepository;
    private final TicketResolutionRepository     ticketResolutionRepository;
    private final TicketConfirmationRepository   ticketConfirmationRepository;
    private final TicketCommentRepository        ticketCommentRepository;
    private final TicketAssignmentHistoryRepository ticketAssignmentHistoryRepository;
    private final AssignmentService              assignmentService;

    // =====================================================================
    // EMPLOYEE: Create ticket
    // =====================================================================

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        User employee = getCurrentUser();

        if (!Boolean.TRUE.equals(employee.getIsActive())) {
            throw new BadRequestException("Account is inactive. Cannot create tickets.");
        }

        IssueCategory category = issueCategoryRepository
                .findByNameAndIsActiveTrue(request.getCategory().toUpperCase())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or inactive category: " + request.getCategory()));

        String ticketNumber = generateTicketNumber();

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .employee(employee)
                .category(category)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .businessUnit(request.getBusinessUnit())
                .workLocation(request.getWorkLocation())
                .assetTag(request.getAssetTag())
                .status(TicketStatus.OPEN)
                .build();

        ticket = ticketRepository.save(ticket);

        // Record OPEN status
        recordStatusChange(ticket, null, TicketStatus.OPEN, employee, "Ticket created");

        // Attempt round-robin assignment
        boolean assigned = assignmentService.assignTicket(ticket, category, null);
        ticket = ticketRepository.save(ticket);

        if (assigned) {
            recordStatusChange(ticket, TicketStatus.OPEN, TicketStatus.ASSIGNED, null,
                    "Auto-assigned via round-robin");
        }

        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // EMPLOYEE: View own tickets
    // =====================================================================

    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets() {
        User employee = getCurrentUser();
        return ticketRepository.findByEmployeeIdOrderByCreatedAtDesc(employee.getId())
                .stream()
                .map(this::buildTicketResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getMyTicketById(Long ticketId) {
        User employee = getCurrentUser();
        Ticket ticket = ticketRepository.findByIdAndEmployeeId(ticketId, employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // EMPLOYEE: Confirm or Reject resolution
    // =====================================================================

    @Transactional
    public TicketResponse confirmResolution(Long ticketId, TicketConfirmationRequest request) {
        User employee = getCurrentUser();
        Ticket ticket = ticketRepository.findByIdAndEmployeeId(ticketId, employee.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new BadRequestException("Ticket is not in RESOLVED state. Current state: " + ticket.getStatus());
        }

        if (request.getDecision() == ConfirmationDecision.REJECTED
                && (request.getReason() == null || request.getReason().isBlank())) {
            throw new BadRequestException("Reason is required when rejecting a resolution.");
        }

        TicketStatus previousStatus = ticket.getStatus();
        TicketStatus newStatus;

        if (request.getDecision() == ConfirmationDecision.CONFIRMED) {
            newStatus = TicketStatus.CLOSED;
            ticket.setClosedAt(LocalDateTime.now());
            log.info("Ticket {} confirmed (CLOSED) by employee {}", ticket.getTicketNumber(),
                    employee.getEmployeeId());
        } else {
            newStatus = TicketStatus.REOPENED;
            log.info("Ticket {} rejected (REOPENED) by employee {}", ticket.getTicketNumber(),
                    employee.getEmployeeId());
        }

        ticket.setStatus(newStatus);
        ticketRepository.save(ticket);

        // Save confirmation record
        TicketConfirmation confirmation = TicketConfirmation.builder()
                .ticket(ticket)
                .employee(employee)
                .decision(request.getDecision())
                .reason(request.getReason())
                .build();
        ticketConfirmationRepository.save(confirmation);

        recordStatusChange(ticket, previousStatus, newStatus, employee,
                request.getDecision() == ConfirmationDecision.CONFIRMED
                        ? "Employee confirmed resolution"
                        : "Employee rejected resolution: " + request.getReason());

        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // ADMIN: View assigned tickets
    // =====================================================================

    @Transactional(readOnly = true)
    public List<TicketResponse> getAssignedTickets() {
        User admin = getCurrentUser();
        return ticketRepository.findByAssignedAdminId(admin.getId())
                .stream()
                .map(this::buildTicketResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getAssignedTicketById(Long ticketId) {
        User admin = getCurrentUser();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        // Security: admin can only view tickets assigned to them
        if (ticket.getAssignedAdminMapping() == null ||
                !ticket.getAssignedAdminMapping().getAdmin().getId().equals(admin.getId())) {
            throw new AccessDeniedException("You are not authorized to view this ticket.");
        }
        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // ADMIN: Update ticket status (IN_PROGRESS)
    // =====================================================================

    @Transactional
    public TicketResponse updateTicketStatusByAdmin(Long ticketId) {
        User admin = getCurrentUser();
        Ticket ticket = getTicketForAdmin(ticketId, admin.getId());

        if (ticket.getStatus() != TicketStatus.ASSIGNED &&
                ticket.getStatus() != TicketStatus.REOPENED) {
            throw new InvalidTicketStatusTransitionException(
                    ticket.getStatus().name(), TicketStatus.IN_PROGRESS.name());
        }

        TicketStatus prev = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticketRepository.save(ticket);

        recordStatusChange(ticket, prev, TicketStatus.IN_PROGRESS, admin, "Admin started working");
        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // ADMIN: Submit resolution
    // =====================================================================

    @Transactional
    public TicketResponse submitResolution(Long ticketId, SubmitResolutionRequest request) {
        User admin = getCurrentUser();
        Ticket ticket = getTicketForAdmin(ticketId, admin.getId());

        if (ticket.getStatus() != TicketStatus.IN_PROGRESS &&
                ticket.getStatus() != TicketStatus.ASSIGNED &&
                ticket.getStatus() != TicketStatus.REOPENED) {
            throw new BadRequestException(
                    "Cannot submit resolution for ticket in status: " + ticket.getStatus());
        }

        TicketStatus prev = ticket.getStatus();
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        String resolutionCode = String.format("%08d", new java.util.Random().nextInt(100000000));

        TicketResolution resolution = TicketResolution.builder()
                .ticket(ticket)
                .resolvedBy(admin)
                .solutionDescription(request.getSolutionDescription())
                .resolutionCode(resolutionCode)
                .build();
        ticketResolutionRepository.save(resolution);

        recordStatusChange(ticket, prev, TicketStatus.RESOLVED, admin, "Admin submitted resolution");
        log.info("Admin {} submitted resolution for ticket {}", admin.getEmployeeId(), ticket.getTicketNumber());

        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // ADMIN: Add comment
    // =====================================================================

    @Transactional
    public CommentResponse addComment(Long ticketId, AddCommentRequest request) {
        User admin = getCurrentUser();
        Ticket ticket = getTicketForAdmin(ticketId, admin.getId());

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(admin)
                .commentText(request.getCommentText())
                .build();
        ticketCommentRepository.save(comment);

        return CommentResponse.builder()
                .id(comment.getId())
                .ticketId(ticket.getId())
                .userId(admin.getId())
                .userName(admin.getFullName())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // =====================================================================
    // ADMIN / MANAGER: Ticket history
    // =====================================================================

    @Transactional(readOnly = true)
    public List<TicketStatusHistory> getStatusHistory(Long ticketId) {
        return ticketStatusHistoryRepository.findByTicketIdOrderByChangedAtAsc(ticketId);
    }

    @Transactional(readOnly = true)
    public List<TicketAssignmentHistory> getAssignmentHistory(Long ticketId) {
        return ticketAssignmentHistoryRepository.findByTicketIdOrderByAssignedAtDesc(ticketId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long ticketId) {
        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .ticketId(ticketId)
                        .userId(c.getUser().getId())
                        .userName(c.getUser().getFullName())
                        .commentText(c.getCommentText())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // =====================================================================
    // MANAGER: Get all tickets with optional filters
    // =====================================================================

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(TicketStatus status, TicketPriority priority,
                                             Long categoryId) {
        return ticketRepository.findByFilters(status, priority, categoryId)
                .stream()
                .map(this::buildTicketResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketByIdForManager(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // MANAGER: Reassign ticket
    // =====================================================================

    @Transactional
    public TicketResponse reassignTicket(Long ticketId, ReassignTicketRequest request,
                                         AdminCategoryAssignment newMapping) {
        User manager = getCurrentUser();
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot reassign a closed ticket.");
        }

        TicketStatus prev = ticket.getStatus();
        assignmentService.reassignTicket(ticket, newMapping, manager);
        ticket = ticketRepository.save(ticket);

        recordStatusChange(ticket, prev, TicketStatus.ASSIGNED, manager,
                "Manager reassigned ticket: " + (request.getReason() != null ? request.getReason() : ""));

        return buildTicketResponse(ticket);
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    private Ticket getTicketForAdmin(Long ticketId, Long adminId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        if (ticket.getAssignedAdminMapping() == null ||
                !ticket.getAssignedAdminMapping().getAdmin().getId().equals(adminId)) {
            throw new AccessDeniedException("You are not authorized to manage this ticket.");
        }
        return ticket;
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void recordStatusChange(Ticket ticket, TicketStatus from, TicketStatus to,
                                    User changedBy, String reason) {
        TicketStatusHistory history = TicketStatusHistory.builder()
                .ticket(ticket)
                .previousStatus(from)
                .newStatus(to)
                .changedBy(changedBy)
                .changeReason(reason)
                .build();
        ticketStatusHistoryRepository.save(history);
    }

    private String generateTicketNumber() {
        // Format: TKT-YYYYMMDD-XXXXX
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        String number = "TKT-" + datePart + "-" + uniquePart;
        // Ensure uniqueness (extremely rare collision, but check anyway)
        while (ticketRepository.existsByTicketNumber(number)) {
            uniquePart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            number = "TKT-" + datePart + "-" + uniquePart;
        }
        return number;
    }

    public TicketResponse buildTicketResponse(Ticket ticket) {
        // Latest resolution
        ResolutionResponse latestResolution = ticketResolutionRepository
                .findTopByTicketIdOrderByResolvedAtDesc(ticket.getId())
                .map(r -> ResolutionResponse.builder()
                        .id(r.getId())
                        .ticketId(ticket.getId())
                        .resolvedById(r.getResolvedBy().getId())
                        .resolvedByName(r.getResolvedBy().getFullName())
                        .solutionDescription(r.getSolutionDescription())
                        .resolutionCode(r.getResolutionCode())
                        .resolvedAt(r.getResolvedAt())
                        .build())
                .orElse(null);

        // Admin info from mapping
        Long   adminId    = null;
        String adminName  = null;
        String adminEmpId = null;
        if (ticket.getAssignedAdminMapping() != null) {
            User admin = ticket.getAssignedAdminMapping().getAdmin();
            adminId    = admin.getId();
            adminName  = admin.getFullName();
            adminEmpId = admin.getEmployeeId();
        }

        String latestRejectionReason = null;
        if (ticket.getStatus() == TicketStatus.REOPENED) {
            List<TicketConfirmation> confirmations = ticketConfirmationRepository.findByTicketIdOrderByConfirmedAtDesc(ticket.getId());
            for (TicketConfirmation conf : confirmations) {
                if (conf.getDecision() == ConfirmationDecision.REJECTED) {
                    latestRejectionReason = conf.getReason();
                    break;
                }
            }
        }

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .employeeId(ticket.getEmployee().getId())
                .employeeName(ticket.getEmployee().getFullName())
                .employeeEmpId(ticket.getEmployee().getEmployeeId())
                .employeeEmail(ticket.getEmployee().getEmail())
                .employeeContactNumber(ticket.getEmployee().getContactNumber())
                .category(ticket.getCategory().getName())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .businessUnit(ticket.getBusinessUnit())
                .workLocation(ticket.getWorkLocation())
                .assetTag(ticket.getAssetTag())
                .assignmentFailureReason(ticket.getAssignmentFailureReason())
                .assignedAdminId(adminId)
                .assignedAdminName(adminName)
                .assignedAdminEmpId(adminEmpId)
                .createdAt(ticket.getCreatedAt())
                .assignedAt(ticket.getAssignedAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .updatedAt(ticket.getUpdatedAt())
                .latestResolution(latestResolution)
                .latestRejectionReason(latestRejectionReason)
                .build();
    }
}
