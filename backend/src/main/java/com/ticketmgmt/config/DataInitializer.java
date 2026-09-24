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
    private final PasswordEncoder            passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedCategories();
        seedAccounts();
        seedAssignmentConfigs();
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

        if (userRepository.findByEmail("hardware_admin@company.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            User hwAdmin = User.builder()
                    .employeeId("ADM001")
                    .fullName("Hardware Admin")
                    .email("hardware_admin@company.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .contactNumber("9000000002")
                    .role(adminRole)
                    .isActive(true)
                    .build();
            userRepository.save(hwAdmin);
            log.info("Seeded default admin account: hardware_admin@company.com / Admin@123");
        }

        if (userRepository.findByEmail("software_admin@company.com").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            User swAdmin = User.builder()
                    .employeeId("ADM002")
                    .fullName("Software Admin")
                    .email("software_admin@company.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .contactNumber("9000000003")
                    .role(adminRole)
                    .isActive(true)
                    .build();
            userRepository.save(swAdmin);
            log.info("Seeded default admin account: software_admin@company.com / Admin@123");
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
}
