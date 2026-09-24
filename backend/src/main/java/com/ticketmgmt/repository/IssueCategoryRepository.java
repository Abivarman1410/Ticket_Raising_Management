package com.ticketmgmt.repository;

import com.ticketmgmt.entity.IssueCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssueCategoryRepository extends JpaRepository<IssueCategory, Long> {
    Optional<IssueCategory> findByName(String name);
    Optional<IssueCategory> findByNameAndIsActiveTrue(String name);
}
