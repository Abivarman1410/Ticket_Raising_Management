package com.ticketmgmt.repository;

import com.ticketmgmt.entity.TicketResolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketResolutionRepository extends JpaRepository<TicketResolution, Long> {
    List<TicketResolution> findByTicketIdOrderByResolvedAtDesc(Long ticketId);
    Optional<TicketResolution> findTopByTicketIdOrderByResolvedAtDesc(Long ticketId);
}
