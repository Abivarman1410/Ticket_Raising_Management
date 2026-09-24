package com.ticketmgmt.repository;

import com.ticketmgmt.entity.AssignmentConfig;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssignmentConfigRepository extends JpaRepository<AssignmentConfig, Long> {

    /**
     * Pessimistic write lock to safely update the round-robin cursor.
     * This prevents concurrent ticket creation from reading the same sequence value.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ac FROM AssignmentConfig ac WHERE ac.category.id = :categoryId")
    Optional<AssignmentConfig> findByCategoryIdWithLock(@Param("categoryId") Long categoryId);

    Optional<AssignmentConfig> findByCategoryId(Long categoryId);
}
