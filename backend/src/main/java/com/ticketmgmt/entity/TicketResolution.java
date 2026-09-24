package com.ticketmgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_resolutions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by", nullable = false)
    private User resolvedBy;

    @Column(name = "solution_description", nullable = false, columnDefinition = "TEXT")
    private String solutionDescription;

    @Column(name = "resolution_code", length = 8)
    private String resolutionCode;

    @CreationTimestamp
    @Column(name = "resolved_at", nullable = false, updatable = false)
    private LocalDateTime resolvedAt;
}
