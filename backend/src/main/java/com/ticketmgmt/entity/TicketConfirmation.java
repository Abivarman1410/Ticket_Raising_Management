package com.ticketmgmt.entity;

import com.ticketmgmt.enums.ConfirmationDecision;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_confirmations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private ConfirmationDecision decision;

    @Column(name = "reason", length = 500)
    private String reason;

    @CreationTimestamp
    @Column(name = "confirmed_at", nullable = false, updatable = false)
    private LocalDateTime confirmedAt;
}
