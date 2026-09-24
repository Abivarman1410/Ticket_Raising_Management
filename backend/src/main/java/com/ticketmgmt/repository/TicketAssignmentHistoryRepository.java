package com.ticketmgmt.repository;

import com.ticketmgmt.entity.TicketAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAssignmentHistoryRepository extends JpaRepository<TicketAssignmentHistory, Long> {
    List<TicketAssignmentHistory> findByTicketIdOrderByAssignedAtDesc(Long ticketId);
    Optional<TicketAssignmentHistory> findByTicketIdAndIsCurrentTrue(Long ticketId);
}
