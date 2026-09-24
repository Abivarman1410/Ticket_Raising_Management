package com.ticketmgmt.service;

import com.ticketmgmt.entity.*;
import com.ticketmgmt.enums.AssignmentMethod;
import com.ticketmgmt.enums.TicketStatus;

import com.ticketmgmt.exception.ResourceNotFoundException;
import com.ticketmgmt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles the round-robin assignment algorithm.
 * Uses pessimistic DB locking to prevent duplicate cursor selection under concurrent load.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentConfigRepository          assignmentConfigRepository;
    private final AdminCategoryAssignmentRepository   adminCategoryAssignmentRepository;
    private final TicketAssignmentHistoryRepository   ticketAssignmentHistoryRepository;

    /**
     * Assign a ticket to the next eligible admin using round-robin.
     * Must be called inside an existing transaction.
     *
     * @param ticket   the ticket to assign
     * @param category the issue category
     * @param assignedBy the user initiating the assignment (null for system)
     */
    @Transactional
    public boolean assignTicket(Ticket ticket, IssueCategory category, User assignedBy) {
        // Acquire the assignment config with a pessimistic write lock
        AssignmentConfig config = assignmentConfigRepository
                .findByCategoryIdWithLock(category.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Assignment config not found for category: " + category.getName()));

        // Get all active eligible admins ordered by their mapping id (deterministic)
        List<AdminCategoryAssignment> eligibleMappings =
                adminCategoryAssignmentRepository.findActiveAdminsForCategory(category.getId());

        if (eligibleMappings.isEmpty()) {
            log.warn("No active eligible admins for category {}. Ticket {} left unassigned.",
                    category.getName(), ticket.getTicketNumber());
            ticket.setAssignmentFailureReason(
                    "No active " + category.getName() + " admins available");
            return false;
        }

        // Round-robin: pick index based on next_sequence mod pool size
        int poolSize = eligibleMappings.size();
        int index    = (int) (config.getNextSequence() % poolSize);
        AdminCategoryAssignment selectedMapping = eligibleMappings.get(index);

        // Update the cursor safely within this locked transaction
        config.setNextSequence(config.getNextSequence() + 1);
        assignmentConfigRepository.save(config);

        // Update ticket
        ticket.setAssignedAdminMapping(selectedMapping);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setAssignmentFailureReason(null);

        // Record assignment history: deactivate previous current assignment if any
        ticketAssignmentHistoryRepository
                .findByTicketIdAndIsCurrentTrue(ticket.getId())
                .ifPresent(prev -> {
                    prev.setIsCurrent(false);
                    prev.setUnassignedAt(LocalDateTime.now());
                    ticketAssignmentHistoryRepository.save(prev);
                });

        // Save new assignment history record
        TicketAssignmentHistory history = TicketAssignmentHistory.builder()
                .ticket(ticket)
                .assignedAdmin(selectedMapping.getAdmin())
                .category(category)
                .assignedBy(assignedBy)
                .assignmentMethod(assignedBy == null
                        ? AssignmentMethod.ROUND_ROBIN
                        : AssignmentMethod.REASSIGNED)
                .isCurrent(true)
                .build();
        ticketAssignmentHistoryRepository.save(history);

        log.info("Ticket {} assigned to admin {} (category: {}) via round-robin index {}",
                ticket.getTicketNumber(),
                selectedMapping.getAdmin().getEmployeeId(),
                category.getName(),
                index);

        return true;
    }

    /**
     * Perform a manager-authorized manual reassignment to a specific admin.
     */
    @Transactional
    public void reassignTicket(Ticket ticket, AdminCategoryAssignment newMapping, User manager) {
        // Deactivate previous assignment history
        ticketAssignmentHistoryRepository
                .findByTicketIdAndIsCurrentTrue(ticket.getId())
                .ifPresent(prev -> {
                    prev.setIsCurrent(false);
                    prev.setUnassignedAt(LocalDateTime.now());
                    ticketAssignmentHistoryRepository.save(prev);
                });

        // Assign to new mapping
        ticket.setAssignedAdminMapping(newMapping);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setAssignmentFailureReason(null);

        // Save new history record
        TicketAssignmentHistory history = TicketAssignmentHistory.builder()
                .ticket(ticket)
                .assignedAdmin(newMapping.getAdmin())
                .category(ticket.getCategory())
                .assignedBy(manager)
                .assignmentMethod(AssignmentMethod.MANUAL)
                .isCurrent(true)
                .build();
        ticketAssignmentHistoryRepository.save(history);

        log.info("Ticket {} manually reassigned to admin {} by manager {}",
                ticket.getTicketNumber(),
                newMapping.getAdmin().getEmployeeId(),
                manager.getEmployeeId());
    }
}
