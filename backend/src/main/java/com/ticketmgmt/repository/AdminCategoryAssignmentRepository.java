package com.ticketmgmt.repository;

import com.ticketmgmt.entity.AdminCategoryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminCategoryAssignmentRepository extends JpaRepository<AdminCategoryAssignment, Long> {

    /**
     * Find active assignment mapping for an admin in a specific category.
     */
    Optional<AdminCategoryAssignment> findByAdminIdAndCategoryIdAndIsActiveTrue(Long adminId, Long categoryId);

    /**
     * Find all active admin-category mappings for a given category, ordered by ID for round-robin.
     * Only includes admins who are themselves active.
     */
    @Query("""
        SELECT aca FROM AdminCategoryAssignment aca
        WHERE aca.category.id = :categoryId
          AND aca.isActive = true
          AND aca.admin.isActive = true
        ORDER BY aca.id ASC
        """)
    List<AdminCategoryAssignment> findActiveAdminsForCategory(@Param("categoryId") Long categoryId);

    /**
     * Find all category assignments for a specific admin.
     */
    List<AdminCategoryAssignment> findByAdminId(Long adminId);

    /**
     * Find all active category assignments for a specific admin.
     */
    List<AdminCategoryAssignment> findByAdminIdAndIsActiveTrue(Long adminId);

    boolean existsByAdminIdAndCategoryId(Long adminId, Long categoryId);
}
