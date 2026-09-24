-- ============================================================
-- V1__init_schema.sql
-- Initial database schema for Organization Ticket Management System
-- ============================================================

-- -----------------------------------------------------------
-- 1. ROLES
-- -----------------------------------------------------------
CREATE TABLE roles (
    id   BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO roles (name) VALUES ('EMPLOYEE'), ('ADMIN'), ('MANAGER');

-- -----------------------------------------------------------
-- 2. USERS
-- -----------------------------------------------------------
CREATE TABLE users (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT,
    employee_id   VARCHAR(50)  NOT NULL UNIQUE,
    full_name     VARCHAR(150) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    contact_number VARCHAR(20),
    role_id       BIGINT       NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Seed manager account (password: Admin@123 — BCrypt hash)
INSERT INTO users (employee_id, full_name, email, password_hash, contact_number, role_id, is_active)
VALUES (
    'MGR001',
    'System Manager',
    'manager@company.com',
    '$2a$12$6c7.WQs3i/Ak1A5EXRV0c.6yQyFmcHoD.VFXfVFBKjWtJ9MMFh7Ga',
    '9000000000',
    (SELECT id FROM roles WHERE name = 'MANAGER'),
    TRUE
);

-- -----------------------------------------------------------
-- 3. ISSUE CATEGORIES
-- -----------------------------------------------------------
CREATE TABLE issue_categories (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO issue_categories (name, description) VALUES
    ('HARDWARE', 'Physical equipment and device issues'),
    ('SOFTWARE', 'Applications and software-related issues');

-- -----------------------------------------------------------
-- 4. ADMIN CATEGORY ASSIGNMENTS
-- -----------------------------------------------------------
CREATE TABLE admin_category_assignments (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    admin_id    BIGINT    NOT NULL,
    category_id BIGINT    NOT NULL,
    is_active   BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (admin_id, category_id),

    CONSTRAINT fk_aca_admin    FOREIGN KEY (admin_id)    REFERENCES users(id),
    CONSTRAINT fk_aca_category FOREIGN KEY (category_id) REFERENCES issue_categories(id)
);

-- -----------------------------------------------------------
-- 5. ASSIGNMENT CONFIGS (one per category)
-- -----------------------------------------------------------
CREATE TABLE assignment_configs (
    id                  BIGINT       PRIMARY KEY AUTO_INCREMENT,
    category_id         BIGINT       NOT NULL UNIQUE,
    next_sequence       BIGINT       NOT NULL DEFAULT 0,
    assignment_strategy VARCHAR(30)  NOT NULL DEFAULT 'ROUND_ROBIN',
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ac_category FOREIGN KEY (category_id) REFERENCES issue_categories(id)
);

-- Seed one config per category
INSERT INTO assignment_configs (category_id, next_sequence, assignment_strategy)
SELECT id, 0, 'ROUND_ROBIN' FROM issue_categories;

-- -----------------------------------------------------------
-- 6. TICKETS
-- -----------------------------------------------------------
CREATE TABLE tickets (
    id                       BIGINT       PRIMARY KEY AUTO_INCREMENT,
    ticket_number            VARCHAR(30)  NOT NULL UNIQUE,
    employee_id              BIGINT       NOT NULL,
    category_id              BIGINT       NOT NULL,
    assigned_admin_mapping_id BIGINT      NULL,
    title                    VARCHAR(200) NOT NULL,
    description              TEXT         NOT NULL,
    priority                 ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status                   ENUM('OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED','REOPENED') NOT NULL DEFAULT 'OPEN',
    assignment_failure_reason VARCHAR(255) NULL,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_at              TIMESTAMP    NULL,
    resolved_at              TIMESTAMP    NULL,
    closed_at                TIMESTAMP    NULL,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tickets_employee      FOREIGN KEY (employee_id)               REFERENCES users(id),
    CONSTRAINT fk_tickets_category      FOREIGN KEY (category_id)               REFERENCES issue_categories(id),
    CONSTRAINT fk_tickets_admin_mapping FOREIGN KEY (assigned_admin_mapping_id) REFERENCES admin_category_assignments(id),

    INDEX idx_tickets_employee          (employee_id),
    INDEX idx_tickets_category_status   (category_id, status),
    INDEX idx_tickets_assigned_status   (assigned_admin_mapping_id, status),
    INDEX idx_tickets_priority_status   (priority, status)
);

-- -----------------------------------------------------------
-- 7. TICKET ASSIGNMENT HISTORY
-- -----------------------------------------------------------
CREATE TABLE ticket_assignment_history (
    id                 BIGINT      PRIMARY KEY AUTO_INCREMENT,
    ticket_id          BIGINT      NOT NULL,
    assigned_admin_id  BIGINT      NOT NULL,
    category_id        BIGINT      NOT NULL,
    assigned_by_user_id BIGINT     NULL,
    assignment_method  VARCHAR(30) NOT NULL,
    assigned_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unassigned_at      TIMESTAMP   NULL,
    is_current         BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_tah_ticket   FOREIGN KEY (ticket_id)           REFERENCES tickets(id),
    CONSTRAINT fk_tah_admin    FOREIGN KEY (assigned_admin_id)   REFERENCES users(id),
    CONSTRAINT fk_tah_category FOREIGN KEY (category_id)         REFERENCES issue_categories(id),
    CONSTRAINT fk_tah_assigner FOREIGN KEY (assigned_by_user_id) REFERENCES users(id),

    INDEX idx_tah_ticket (ticket_id),
    INDEX idx_tah_admin  (assigned_admin_id)
);

-- -----------------------------------------------------------
-- 8. TICKET STATUS HISTORY
-- -----------------------------------------------------------
CREATE TABLE ticket_status_history (
    id                BIGINT      PRIMARY KEY AUTO_INCREMENT,
    ticket_id         BIGINT      NOT NULL,
    previous_status   VARCHAR(20) NULL,
    new_status        VARCHAR(20) NOT NULL,
    changed_by_user_id BIGINT     NULL,
    change_reason     VARCHAR(255),
    changed_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tsh_ticket  FOREIGN KEY (ticket_id)          REFERENCES tickets(id),
    CONSTRAINT fk_tsh_changer FOREIGN KEY (changed_by_user_id) REFERENCES users(id),

    INDEX idx_tsh_ticket (ticket_id)
);

-- -----------------------------------------------------------
-- 9. TICKET RESOLUTIONS
-- -----------------------------------------------------------
CREATE TABLE ticket_resolutions (
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id            BIGINT NOT NULL,
    resolved_by          BIGINT NOT NULL,
    solution_description TEXT   NOT NULL,
    resolved_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tr_ticket   FOREIGN KEY (ticket_id)   REFERENCES tickets(id),
    CONSTRAINT fk_tr_resolver FOREIGN KEY (resolved_by) REFERENCES users(id),

    INDEX idx_tr_ticket (ticket_id)
);

-- -----------------------------------------------------------
-- 10. TICKET CONFIRMATIONS
-- -----------------------------------------------------------
CREATE TABLE ticket_confirmations (
    id           BIGINT      PRIMARY KEY AUTO_INCREMENT,
    ticket_id    BIGINT      NOT NULL,
    employee_id  BIGINT      NOT NULL,
    decision     ENUM('CONFIRMED','REJECTED') NOT NULL,
    reason       VARCHAR(500),
    confirmed_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tc_ticket   FOREIGN KEY (ticket_id)   REFERENCES tickets(id),
    CONSTRAINT fk_tc_employee FOREIGN KEY (employee_id) REFERENCES users(id),

    INDEX idx_tc_ticket (ticket_id)
);

-- -----------------------------------------------------------
-- 11. TICKET COMMENTS
-- -----------------------------------------------------------
CREATE TABLE ticket_comments (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    ticket_id    BIGINT NOT NULL,
    user_id      BIGINT NOT NULL,
    comment_text TEXT   NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tco_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id),
    CONSTRAINT fk_tco_user   FOREIGN KEY (user_id)   REFERENCES users(id),

    INDEX idx_tco_ticket (ticket_id)
);
