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
        if (userRepository.findByEmail("manager@company.com").isEmpty()) {
            Role managerRole = roleRepository.findByName("MANAGER").orElseThrow();
            User manager = User.builder()
                    .employeeId("MGR001")
                    .fullName("System Manager")
                    .email("manager@company.com")
                    .passwordHash(passwordEncoder.encode("Manager@123"))
                    .contactNumber("9000000000")
                    .role(managerRole)
                    .isActive(true)
                    .build();
            userRepository.save(manager);
            log.info("Seeded default manager account: manager@company.com / Manager@123");
        }

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
