package com.ticketmgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false, unique = true)
    private IssueCategory category;

    @Column(name = "next_sequence", nullable = false)
    @Builder.Default
    private Long nextSequence = 0L;

    @Column(name = "assignment_strategy", nullable = false, length = 30)
    @Builder.Default
    private String assignmentStrategy = "ROUND_ROBIN";

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
