package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.CreateAdminRequest;
import com.ticketmgmt.dto.request.UpdateAdminCategoryRequest;
import com.ticketmgmt.dto.request.UpdateAdminStatusRequest;
import com.ticketmgmt.dto.response.AdminResponse;
import com.ticketmgmt.dto.response.DashboardResponse;
import com.ticketmgmt.entity.*;
import com.ticketmgmt.enums.TicketPriority;
import com.ticketmgmt.enums.TicketStatus;
import com.ticketmgmt.exception.*;
import com.ticketmgmt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerService {

    private final UserRepository                    userRepository;
    private final RoleRepository                    roleRepository;
    private final IssueCategoryRepository           issueCategoryRepository;
    private final AdminCategoryAssignmentRepository adminCategoryAssignmentRepository;
    private final TicketRepository                  ticketRepository;
    private final PasswordEncoder                   passwordEncoder;

    // =====================================================================
    // Admin Account Management
    // =====================================================================

    @Transactional
    public AdminResponse createAdmin(CreateAdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("Employee ID already exists: " + request.getEmployeeId());
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new ResourceNotFoundException("ADMIN role not found"));

        IssueCategory category = issueCategoryRepository
                .findByNameAndIsActiveTrue(request.getCategory().toUpperCase())
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or inactive category: " + request.getCategory()));

        User admin = User.builder()
                .employeeId(request.getEmployeeId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .contactNumber(request.getContactNumber())
                .role(adminRole)
                .isActive(true)
                .build();
        admin = userRepository.save(admin);

        // Assign initial category
        AdminCategoryAssignment mapping = AdminCategoryAssignment.builder()
                .admin(admin)
                .category(category)
                .isActive(true)
                .build();
        adminCategoryAssignmentRepository.save(mapping);

        log.info("Manager created admin account: {} ({})", admin.getEmployeeId(), category.getName());
        return buildAdminResponse(admin);
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return userRepository.findByRoleName("ADMIN")
                .stream()
                .map(this::buildAdminResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminResponse getAdminById(Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminId));
        validateIsAdmin(admin);
        return buildAdminResponse(admin);
    }

    @Transactional
    public AdminResponse updateAdminStatus(Long adminId, UpdateAdminStatusRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminId));
        validateIsAdmin(admin);

        admin.setIsActive(request.getIsActive());
        userRepository.save(admin);

        // If deactivated, also deactivate all category assignments so they don't receive new tickets
        if (!request.getIsActive()) {
            List<AdminCategoryAssignment> mappings =
                    adminCategoryAssignmentRepository.findByAdminId(adminId);
            mappings.forEach(m -> m.setIsActive(false));
            adminCategoryAssignmentRepository.saveAll(mappings);
        }

        log.info("Admin {} active status set to {}", admin.getEmployeeId(), request.getIsActive());
        return buildAdminResponse(admin);
    }

    @Transactional
    public AdminResponse addCategoryToAdmin(Long adminId, UpdateAdminCategoryRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminId));
        validateIsAdmin(admin);

        IssueCategory category = issueCategoryRepository
                .findByNameAndIsActiveTrue(request.getCategory().toUpperCase())
                .orElseThrow(() -> new BadRequestException("Invalid category: " + request.getCategory()));

        if (adminCategoryAssignmentRepository.existsByAdminIdAndCategoryId(adminId, category.getId())) {
            // Update existing mapping
            AdminCategoryAssignment existing = adminCategoryAssignmentRepository
                    .findByAdminIdAndCategoryIdAndIsActiveTrue(adminId, category.getId())
                    .orElseGet(() -> AdminCategoryAssignment.builder()
                            .admin(admin).category(category).build());
            existing.setIsActive(true);
            adminCategoryAssignmentRepository.save(existing);
        } else {
            AdminCategoryAssignment mapping = AdminCategoryAssignment.builder()
                    .admin(admin)
                    .category(category)
                    .isActive(true)
                    .build();
            adminCategoryAssignmentRepository.save(mapping);
        }

        log.info("Category {} added to admin {}", category.getName(), admin.getEmployeeId());
        return buildAdminResponse(admin);
    }

    @Transactional
    public void removeCategoryFromAdmin(Long adminId, Long categoryId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + adminId));
        validateIsAdmin(admin);

        AdminCategoryAssignment mapping = adminCategoryAssignmentRepository
                .findByAdminIdAndCategoryIdAndIsActiveTrue(adminId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category assignment not found for admin " + adminId));

        mapping.setIsActive(false);
        adminCategoryAssignmentRepository.save(mapping);
        log.info("Category {} removed from admin {}", categoryId, admin.getEmployeeId());
    }

    // =====================================================================
    // Dashboard
    // =====================================================================

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardMetrics() {
        Map<String, Long> ticketsByPriority = new LinkedHashMap<>();
        for (TicketPriority p : TicketPriority.values()) {
            ticketsByPriority.put(p.name(), ticketRepository.countByPriority(p));
        }

        Map<String, Long> ticketsByCategory = new LinkedHashMap<>();
        issueCategoryRepository.findAll().forEach(cat ->
                ticketsByCategory.put(cat.getName(), ticketRepository.countByCategoryId(cat.getId())));

        // Admin workload: active tickets per admin
        Map<String, Long> adminWorkload = new LinkedHashMap<>();
        userRepository.findByRoleName("ADMIN").forEach(admin -> {
            long count = ticketRepository.findByAssignedAdminId(admin.getId())
                    .stream()
                    .filter(t -> t.getStatus() != TicketStatus.CLOSED)
                    .count();
            adminWorkload.put(admin.getFullName() + " (" + admin.getEmployeeId() + ")", count);
        });

        // Tickets with no admin (OPEN but unassigned)
        long unassigned = ticketRepository.findByFilters(TicketStatus.OPEN, null, null)
                .stream()
                .filter(t -> t.getAssignedAdminMapping() == null)
                .count();

        return DashboardResponse.builder()
                .totalTickets(ticketRepository.count())
                .openTickets(ticketRepository.countByStatus(TicketStatus.OPEN))
                .assignedTickets(ticketRepository.countByStatus(TicketStatus.ASSIGNED))
                .inProgressTickets(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS))
                .resolvedTickets(ticketRepository.countByStatus(TicketStatus.RESOLVED))
                .closedTickets(ticketRepository.countByStatus(TicketStatus.CLOSED))
                .reopenedTickets(ticketRepository.countByStatus(TicketStatus.REOPENED))
                .ticketsByPriority(ticketsByPriority)
                .ticketsByCategory(ticketsByCategory)
                .adminWorkload(adminWorkload)
                .unassignedTickets(unassigned)
                .build();
    }

    // =====================================================================
    // Private helpers
    // =====================================================================

    private void validateIsAdmin(User user) {
        if (!"ADMIN".equals(user.getRole().getName())) {
            throw new BadRequestException("User " + user.getEmployeeId() + " is not an admin.");
        }
    }

    private AdminResponse buildAdminResponse(User admin) {
        List<String> categories = adminCategoryAssignmentRepository
                .findByAdminIdAndIsActiveTrue(admin.getId())
                .stream()
                .map(m -> m.getCategory().getName())
                .collect(Collectors.toList());

        return AdminResponse.builder()
                .id(admin.getId())
                .employeeId(admin.getEmployeeId())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .contactNumber(admin.getContactNumber())
                .isActive(admin.getIsActive())
                .categories(categories)
                .createdAt(admin.getCreatedAt())
                .build();
    }
}
