package com.ticketmgmt.config;

import com.ticketmgmt.entity.*;
import com.ticketmgmt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the database with required initial data (roles, categories, assignment configs,
 * and a default manager account) if they do not already exist.
 *
 * This runs on application startup and is idempotent — safe to run on a populated database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository             roleRepository;
    private final UserRepository             userRepository;
    private final IssueCategoryRepository    issueCategoryRepository;
    private final AssignmentConfigRepository assignmentConfigRepository;
    private final AdminCategoryAssignmentRepository adminCategoryAssignmentRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder            passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedCategories();
        seedAccounts();
        seedAssignmentConfigs();
        seedTickets();
        log.info("DataInitializer: seed data verified/applied successfully.");
    }

    private void seedRoles() {
        for (String roleName : new String[]{"EMPLOYEE", "ADMIN", "MANAGER"}) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Seeded role: {}", roleName);
            }
        }
    }

    private void seedCategories() {
        if (issueCategoryRepository.findByName("HARDWARE").isEmpty()) {
            issueCategoryRepository.save(IssueCategory.builder()
                    .name("HARDWARE")
                    .description("Physical equipment and device issues")
                    .isActive(true)
                    .build());
            log.info("Seeded category: HARDWARE");
        }
        if (issueCategoryRepository.findByName("SOFTWARE").isEmpty()) {
            issueCategoryRepository.save(IssueCategory.builder()
                    .name("SOFTWARE")
                    .description("Applications and software-related issues")
                    .isActive(true)
                    .build());
            log.info("Seeded category: SOFTWARE");
        }
    }

    private void seedAccounts() {
        Role managerRole = roleRepository.findByName("MANAGER").orElseThrow();
        User manager = userRepository.findByEmail("manager@company.com").orElseGet(() -> 
            User.builder()
                .employeeId("MGR001")
                .email("manager@company.com")
                .role(managerRole)
                .isActive(true)
                .build()
        );
        manager.setFullName("System Manager");
        manager.setContactNumber("9000000000");
        manager.setPasswordHash(passwordEncoder.encode("Manager@123"));
        userRepository.save(manager);
        log.info("Seeded/Reset default manager account: manager@company.com / Manager@123");

        if (userRepository.findByEmail("employee@company.com").isEmpty()) {
            Role employeeRole = roleRepository.findByName("EMPLOYEE").orElseThrow();
            User employee = User.builder()
                    .employeeId("EMP001")
                    .fullName("Test Employee")
                    .email("employee@company.com")
                    .passwordHash(passwordEncoder.encode("Employee@123"))
                    .contactNumber("9000000001")
                    .role(employeeRole)
                    .isActive(true)
                    .build();
            userRepository.save(employee);
            log.info("Seeded default employee account: employee@company.com / Employee@123");
        }

        // HARDWARE ADMIN
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        User hwAdmin = userRepository.findByEmail("hardware_admin@company.com").orElseGet(() -> {
            User newUser = User.builder()
                    .employeeId("ADM002")
                    .fullName("Hardware Admin")
                    .email("hardware_admin@company.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .contactNumber("9000000002")
                    .role(adminRole)
                    .isActive(true)
                    .build();
            return userRepository.save(newUser);
        });

        IssueCategory hwCategory = issueCategoryRepository.findByName("HARDWARE").orElseThrow();
        if (adminCategoryAssignmentRepository.findActiveAdminsForCategory(hwCategory.getId()).stream()
                .noneMatch(mapping -> mapping.getAdmin().getId().equals(hwAdmin.getId()))) {
            adminCategoryAssignmentRepository.save(AdminCategoryAssignment.builder()
                    .admin(hwAdmin)
                    .category(hwCategory)
                    .isActive(true)
                    .build());
            log.info("Mapped hardware_admin to HARDWARE category");
        }

        // SOFTWARE ADMIN
        User swAdmin = userRepository.findByEmail("software_admin@company.com").orElseGet(() -> {
            User newUser = User.builder()
                    .employeeId("ADM003")
                    .fullName("Software Admin")
                    .email("software_admin@company.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .contactNumber("9000000003")
                    .role(adminRole)
                    .isActive(true)
                    .build();
            return userRepository.save(newUser);
        });

        IssueCategory swCategory = issueCategoryRepository.findByName("SOFTWARE").orElseThrow();
        if (adminCategoryAssignmentRepository.findActiveAdminsForCategory(swCategory.getId()).stream()
                .noneMatch(mapping -> mapping.getAdmin().getId().equals(swAdmin.getId()))) {
            adminCategoryAssignmentRepository.save(AdminCategoryAssignment.builder()
                    .admin(swAdmin)
                    .category(swCategory)
                    .isActive(true)
                    .build());
            log.info("Mapped software_admin to SOFTWARE category");
        }
    }

    private void seedAssignmentConfigs() {
        issueCategoryRepository.findAll().forEach(category -> {
            if (assignmentConfigRepository.findByCategoryId(category.getId()).isEmpty()) {
                assignmentConfigRepository.save(AssignmentConfig.builder()
                        .category(category)
                        .nextSequence(0L)
                        .assignmentStrategy("ROUND_ROBIN")
                        .build());
                log.info("Seeded assignment config for category: {}", category.getName());
            }
        });
    }

    private void seedTickets() {
        if (ticketRepository.count() == 0) {
            User employee = userRepository.findByEmail("employee@company.com").orElseThrow();
            IssueCategory hwCategory = issueCategoryRepository.findByName("HARDWARE").orElseThrow();
            IssueCategory swCategory = issueCategoryRepository.findByName("SOFTWARE").orElseThrow();

            AdminCategoryAssignment hwMapping = adminCategoryAssignmentRepository.findActiveAdminsForCategory(hwCategory.getId()).get(0);
            AdminCategoryAssignment swMapping = adminCategoryAssignmentRepository.findActiveAdminsForCategory(swCategory.getId()).get(0);

            Ticket hwTicket = Ticket.builder()
                    .ticketNumber("TKT-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-HW001")
                    .employee(employee)
                    .category(hwCategory)
                    .title("Laptop screen flickering")
                    .description("My laptop screen is flickering randomly.")
                    .priority(com.ticketmgmt.enums.TicketPriority.HIGH)
                    .businessUnit("IT")
                    .workLocation("Office")
                    .status(com.ticketmgmt.enums.TicketStatus.ASSIGNED)
                    .assignedAdminMapping(hwMapping)
                    .assignedAt(java.time.LocalDateTime.now())
                    .build();
            ticketRepository.save(hwTicket);

            Ticket swTicket = Ticket.builder()
                    .ticketNumber("TKT-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-SW001")
                    .employee(employee)
                    .category(swCategory)
                    .title("Cannot access VPN")
                    .description("The VPN client fails to connect with an authentication error.")
                    .priority(com.ticketmgmt.enums.TicketPriority.MEDIUM)
                    .businessUnit("HR")
                    .workLocation("Remote")
                    .status(com.ticketmgmt.enums.TicketStatus.ASSIGNED)
                    .assignedAdminMapping(swMapping)
                    .assignedAt(java.time.LocalDateTime.now())
                    .build();
            ticketRepository.save(swTicket);

            log.info("Seeded initial tickets for testing");
        }
    }
}
