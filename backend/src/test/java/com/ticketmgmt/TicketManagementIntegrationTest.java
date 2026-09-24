package com.ticketmgmt;

import com.ticketmgmt.entity.*;
import com.ticketmgmt.exception.*;
import com.ticketmgmt.repository.*;
import com.ticketmgmt.service.*;
import com.ticketmgmt.dto.request.*;
import com.ticketmgmt.dto.response.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for the backend service layer.
 * Uses test profile with H2 in-memory database.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketManagementIntegrationTest {

    @Autowired RoleRepository roleRepository;
    @Autowired UserRepository userRepository;
    @Autowired IssueCategoryRepository issueCategoryRepository;
    @Autowired AdminCategoryAssignmentRepository acaRepository;
    @Autowired AssignmentConfigRepository assignmentConfigRepository;
    @Autowired TicketRepository ticketRepository;
    @Autowired TicketStatusHistoryRepository statusHistoryRepository;
    @Autowired TicketResolutionRepository resolutionRepository;
    @Autowired TicketConfirmationRepository confirmationRepository;
    @Autowired ManagerService managerService;

    @Test
    @Order(1)
    void rolesAreSeededed() {
        assertThat(roleRepository.findByName("EMPLOYEE")).isPresent();
        assertThat(roleRepository.findByName("ADMIN")).isPresent();
        assertThat(roleRepository.findByName("MANAGER")).isPresent();
    }

    @Test
    @Order(2)
    void categoriesAreSeeded() {
        assertThat(issueCategoryRepository.findByName("HARDWARE")).isPresent();
        assertThat(issueCategoryRepository.findByName("SOFTWARE")).isPresent();
    }

    @Test
    @Order(3)
    void assignmentConfigsAreSeeded() {
        IssueCategory hw = issueCategoryRepository.findByName("HARDWARE").orElseThrow();
        IssueCategory sw = issueCategoryRepository.findByName("SOFTWARE").orElseThrow();
        assertThat(assignmentConfigRepository.findByCategoryId(hw.getId())).isPresent();
        assertThat(assignmentConfigRepository.findByCategoryId(sw.getId())).isPresent();
    }

    @Test
    @Order(4)
    void managerSeedAccountExists() {
        assertThat(userRepository.findByEmail("manager@company.com")).isPresent();
    }

    @Test
    @Order(5)
    void managerCanCreateAdmin() {
        CreateAdminRequest req = new CreateAdminRequest();
        req.setEmployeeId("ADM-TEST-001");
        req.setFullName("Test Admin");
        req.setEmail("testadmin@company.com");
        req.setPassword("Admin@1234");
        req.setCategory("HARDWARE");

        AdminResponse response = managerService.createAdmin(req);

        assertThat(response.getEmployeeId()).isEqualTo("ADM-TEST-001");
        assertThat(response.getCategories()).contains("HARDWARE");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @Order(6)
    void duplicateEmailThrowsException() {
        CreateAdminRequest req = new CreateAdminRequest();
        req.setEmployeeId("ADM-DUP-001");
        req.setFullName("Duplicate");
        req.setEmail("testadmin@company.com"); // same email as order-5 test
        req.setPassword("Admin@1234");
        req.setCategory("HARDWARE");

        assertThatThrownBy(() -> managerService.createAdmin(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @Order(7)
    void managerCanListAdmins() {
        List<AdminResponse> admins = managerService.getAllAdmins();
        assertThat(admins).isNotEmpty();
    }

    @Test
    @Order(8)
    void managerCanDeactivateAdmin() {
        User admin = userRepository.findByEmployeeId("ADM-TEST-001").orElseThrow();

        UpdateAdminStatusRequest req = new UpdateAdminStatusRequest();
        req.setIsActive(false);
        AdminResponse result = managerService.updateAdminStatus(admin.getId(), req);

        assertThat(result.getIsActive()).isFalse();
        // Category mapping should also be deactivated
        assertThat(acaRepository.findByAdminIdAndIsActiveTrue(admin.getId())).isEmpty();
    }

    @Test
    @Order(9)
    void dashboardMetricsAreAvailable() {
        DashboardResponse dashboard = managerService.getDashboardMetrics();
        assertThat(dashboard.getTotalTickets()).isGreaterThanOrEqualTo(0);
        assertThat(dashboard.getTicketsByPriority()).isNotNull();
        assertThat(dashboard.getTicketsByCategory()).isNotNull();
    }
}
