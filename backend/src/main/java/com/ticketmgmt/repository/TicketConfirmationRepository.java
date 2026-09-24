package com.ticketmgmt.repository;

import com.ticketmgmt.entity.TicketConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketConfirmationRepository extends JpaRepository<TicketConfirmation, Long> {
    List<TicketConfirmation> findByTicketIdOrderByConfirmedAtDesc(Long ticketId);
}
