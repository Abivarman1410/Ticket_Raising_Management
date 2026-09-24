package com.ticketmgmt.repository;

import com.ticketmgmt.entity.Ticket;
import com.ticketmgmt.enums.TicketPriority;
import com.ticketmgmt.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    // Employee: own tickets
    List<Ticket> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    Optional<Ticket> findByIdAndEmployeeId(Long ticketId, Long employeeId);

    // Admin: tickets assigned via the mapping
    @Query("""
        SELECT t FROM Ticket t
        WHERE t.assignedAdminMapping.admin.id = :adminId
        ORDER BY t.createdAt DESC
        """)
    List<Ticket> findByAssignedAdminId(@Param("adminId") Long adminId);

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.assignedAdminMapping.admin.id = :adminId
          AND t.status = :status
        ORDER BY t.createdAt DESC
        """)
    List<Ticket> findByAssignedAdminIdAndStatus(@Param("adminId") Long adminId,
                                                @Param("status") TicketStatus status);

    // Manager dashboard counts
    long countByStatus(TicketStatus status);

    long countByPriority(TicketPriority priority);

    long countByCategoryId(Long categoryId);

    boolean existsByTicketNumber(String ticketNumber);

    // Manager: filter tickets
    @Query("""
        SELECT t FROM Ticket t
        WHERE (:status IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:categoryId IS NULL OR t.category.id = :categoryId)
        ORDER BY t.createdAt DESC
        """)
    List<Ticket> findByFilters(@Param("status") TicketStatus status,
                               @Param("priority") TicketPriority priority,
                               @Param("categoryId") Long categoryId);
}
