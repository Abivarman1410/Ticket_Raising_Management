Organization Ticket Management System
Finalized System Requirements & Database Design

Macha Abivarman! 🔥 Let's finalize your project into a proper Software Requirements Specification (SRS) + Database Design that you can use as the foundation for your React + Spring Boot + MySQL full-stack application.

Your selected design is:

Final Project Configuration
Confirmed

Frontend

React.js

Backend

Spring Boot (Java)

Database

MySQL

User Roles

Employee, Admin, Manager

Admin Categories

Hardware + Software

Assignment

Round-robin

Manager Visibility

All organization tickets

Resolution

Employee confirmation + reopening

Priority

Low, Medium, High, Critical

Login

Role-selection portal

Part 1: Complete System Requirements (SRS)
1. Project title

Organization Ticket Management and Resolution System

1.1 Project objective

The system provides a centralized platform for employees to report organizational problems, automatically assign tickets to the appropriate hardware or software admin using round-robin allocation, and allow managers to monitor the entire ticket lifecycle.

The system will support:

Employee issue reporting.

Automatic admin assignment.

Admin ticket resolution.

Employee resolution confirmation.

Manager-wide ticket monitoring.

Priority-based ticket management.

Ticket history and audit tracking.

1.2 Problem statement

In an organization with many employees, hardware and software problems may be reported through informal channels such as messages, phone calls, or email. This can make it difficult to track ticket ownership, identify responsible admins, monitor pending issues, and verify whether problems have been resolved.

The proposed application centralizes these operations in a role-based ticket management system.

1.3 Scope

In scope: Employee ticket creation, category-specific assignment, admin resolution, employee confirmation, manager dashboards, role-based security, ticket history, and priority management.

Out of initial scope: Automatic email notifications, mobile application, AI ticket classification, multi-company tenancy, and advanced SLA automation. These can be added later.

Part 2: User Roles and Permissions

The system has three primary roles. Hardware Admin and Software Admin are specializations of the Admin role.

2.1 Permission matrix

Function

	

Employee

	

Admin

	

Manager




Login

	

✓

	

✓

	

✓




Create ticket

	

✓

	

Optional

	

Optional




View own tickets

	

✓

	

Optional

	

✓




View assigned tickets

	

—

	

✓

	

✓




Update ticket status

	

—

	

✓

	

Controlled




Submit resolution

	

—

	

✓

	

—




Confirm resolution

	

✓

	

—

	

—




Reopen ticket

	

✓

	

—

	

Controlled




View all tickets

	

—

	

—

	

✓




Manage admin accounts

	

—

	

—

	

✓




Configure assignment

	

—

	

—

	

✓




View organization statistics

	

—

	

Limited

	

✓




Manage system settings

	

—

	

—

	

✓

Security rule: The backend determines permissions from the authenticated account and its assigned roles. Selecting a login button must never grant access to another role.

2.2 Employee requirements
Functional requirements

Employee can log in through the Employee portal.

Employee can create a new ticket.

Employee must select an issue category:

Hardware

Software

Employee can enter the issue title and description.

Employee can set the issue priority, subject to the chosen priority policy.

Employee can view their own tickets and current statuses.

Employee can view assigned admin details that the organization permits them to see.

Employee can review admin resolution notes.

Employee can confirm that the problem is solved.

Employee can indicate that the issue is not solved, which reopens the ticket.

Employee can view ticket history and timestamps.

Employee ticket form

Field

	

Required

	

Description




Issue category

	

Yes

	

Hardware or Software




Title

	

Yes

	

Short problem summary




Description

	

Yes

	

Detailed issue description




Priority

	

Yes

	

Low / Medium / High / Critical




Attachment

	

Optional

	

Supporting screenshot or document




Contact information

	

System

	

Retrieved from user profile

Recommendation: Do not require employees to re-enter their employee ID and contact number on every ticket. Fetch those details from the authenticated user account and preserve relevant historical information.

2.3 Admin requirements
Hardware Admin

Log in through the Hardware Admin portal.

View assigned hardware tickets.

View employee details authorized for support.

Change ticket status to In Progress.

Add comments or investigation notes.

Submit resolution details.

Mark a ticket as Resolved.

View tickets that have been reopened.

Software Admin

The same functionality applies to software tickets, but the admin should only receive tickets belonging to the supported category unless explicitly authorized for multiple categories.

Admin restrictions

Admin cannot view all organizational tickets by default.

Admin cannot change another admin's ticket assignment unless an authorized workflow allows it.

Admin cannot mark a ticket as Closed on behalf of the employee.

Admin cannot modify the ticket creator after creation.

Admin cannot bypass category assignment rules through the frontend.

2.4 Manager requirements

The manager has organization-wide monitoring access.

Admin management

Create admin accounts.

Assign admin category: Hardware or Software.

Activate or deactivate admins.

View the number of active admins in each category.

View assigned workload.

Configure the round-robin assignment pool.

Reassign existing tickets through an authorized workflow, if needed.

Ticket management

View all organization tickets.

Filter by issue category.

Filter by priority.

Filter by status.

Search by ticket number, employee ID, or assigned admin.

View assignment details.

View resolution details.

View employee confirmation.

Track reopened tickets.

Dashboard metrics

Total tickets.

Open tickets.

In-progress tickets.

Resolved tickets awaiting employee confirmation.

Closed tickets.

Reopened tickets.

Tickets grouped by priority.

Tickets grouped by hardware/software category.

Admin workload summary.

Part 3: Complete Ticket Lifecycle
3.1 Status definitions

Use a controlled status enum in the backend.

Status

	

Meaning




OPEN

	

Employee created a ticket, but assignment has not completed




ASSIGNED

	

Ticket is mapped to an eligible admin




IN_PROGRESS

	

Admin is working on the issue




RESOLVED

	

Admin submitted a solution




CLOSED

	

Employee confirmed the solution




REOPENED

	

Employee reported that the problem persists

Status transition rules
Invalid or unsupported diagram.
Business rules

A ticket must have a valid employee creator.

A ticket must have a supported category.

Only eligible active admins can receive new assignments.

Admins can submit resolution details only for tickets assigned to them, subject to authorized reassignment rules.

Employees can confirm or reject a resolution only for their own tickets.

A rejected resolution changes the ticket to REOPENED.

A ticket must not be closed solely because an admin marked it resolved.

Status changes must be recorded in a history table.

A closed ticket should not be edited as if it were still active. A separate authorized reopen workflow may be added for post-closure issues.

The system should retain the original assigned admin even if a reopened ticket is reassigned.

Part 4: Round-Robin Assignment System
4.1 Assignment rules

The system has two separate assignment pools:

Hardware Admin Pool

Software Admin Pool

When a ticket is created:

Validate the category.

Retrieve active admins eligible for that category.

Acquire the assignment state safely.

Select the next admin in the round-robin sequence.

Assign the ticket.

Update the assignment cursor.

Save the ticket and assignment record within a transaction.

If no eligible admin is available, the ticket should remain unassigned or enter a clearly defined PENDING_ASSIGNMENT workflow. Since your current status list doesn't include this status, you can use OPEN with an assignment failure reason, or add a dedicated status later.

4.2 Example: Hardware round-robin

The manager configures three active hardware admins.

Hardware Admin Pool
3 Active

H001 — Arun

Admin 1

H002 — Karthik

Admin 2

H003 — Priya

Admin 3
Assignment sequence

Ticket T001

H001

Ticket T002

H002

Ticket T003

H003

Ticket T004

H001

Important implementation decision

Use an explicit assignment state per category rather than deriving the next admin from the total ticket count.

Why?

Admins can be added or removed.

Some admins can become inactive.

Multiple employees can submit tickets at the same time.

Existing assignments should not change when the pool changes.

The next assignment must be safely coordinated in the database.

A database transaction with appropriate locking is needed to prevent duplicate assignment cursor updates under concurrent requests.

Part 5: Database Design (MySQL)
5.1 Database name
CREATE DATABASE organization_ticket_management;

USE organization_ticket_management;
Database design principles

Use primary keys for all core entities.

Use foreign keys to enforce relationships.

Use unique constraints for employee IDs, ticket numbers, and usernames/emails where applicable.

Store timestamps in a consistent format.

Use indexes for frequent queries.

Avoid storing derived information such as total admin count in every ticket.

Use transaction boundaries for ticket creation and assignment.

5.2 Entity Relationship Diagram
Invalid or unsupported diagram.
Entity overview

Entity

	

Purpose




roles

	

Defines employee, admin, and manager roles




users

	

Stores all user accounts




issue_categories

	

Defines hardware and software issue types




admin_category_assignments

	

Links admins to supported categories




assignment_configs

	

Stores assignment cursor per category




tickets

	

Main ticket record




ticket_status_history

	

Historical status changes




ticket_resolutions

	

Admin-provided resolution details




ticket_confirmations

	

Employee confirmation decisions




ticket_comments

	

Ticket discussion




ticket_assignment_history

	

Assignment audit trail

Part 6: SQL Table Structure

The following schema is a recommended starting design. You can implement it using Spring Data JPA entities and migrations (Flyway or Liquibase).

6.1 Roles
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

Initial roles:

INSERT INTO roles (name)
VALUES ('EMPLOYEE'), ('ADMIN'), ('MANAGER');

The admin category (Hardware or Software) should be modeled separately from the general role.

6.2 Users
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    employee_id VARCHAR(50) NOT NULL UNIQUE,

    full_name VARCHAR(150) NOT NULL,

    email VARCHAR(150) NOT NULL UNIQUE,

    password_hash VARCHAR(255) NOT NULL,

    contact_number VARCHAR(20),

    role_id BIGINT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
);
Design notes

employee_id can be used for employees and admins if your organization uses employee IDs for all staff.

For a manager or other account without an employee ID, you can either retain a separate unique username field or define the identifier policy according to your organization.

password_hash must store a secure password hash, not a plaintext password.

is_active controls whether an account can log in and receive assignments.

6.3 Issue categories
CREATE TABLE issue_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    name VARCHAR(50) NOT NULL UNIQUE,

    description VARCHAR(255),

    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

Initial values:

INSERT INTO issue_categories (name, description)
VALUES
('HARDWARE', 'Physical equipment and device issues'),
('SOFTWARE', 'Applications and software-related issues');

You can expand categories later if needed, such as Network, Access, or Facilities, but the initial project will support Hardware and Software.

6.4 Admin category assignments

This table maps admin accounts to the issue categories they are eligible to handle.

CREATE TABLE admin_category_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    admin_id BIGINT NOT NULL,

    category_id BIGINT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (admin_id, category_id),

    CONSTRAINT fk_admin_assignment_admin
        FOREIGN KEY (admin_id)
        REFERENCES users(id),

    CONSTRAINT fk_admin_assignment_category
        FOREIGN KEY (category_id)
        REFERENCES issue_categories(id)
);
Business rules

Only users with the Admin role should be added as admins.

An admin can have one or both categories if the system supports that.

For your initial implementation, each admin may be assigned to one category.

The backend must validate role and category eligibility before creating or updating this mapping.

This table is more flexible than hardcoding separate hardware_admin_id and software_admin_id columns.

6.5 Assignment configuration

Use one assignment configuration record per category.

CREATE TABLE assignment_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    category_id BIGINT NOT NULL UNIQUE,

    next_sequence BIGINT NOT NULL DEFAULT 0,

    assignment_strategy VARCHAR(30) NOT NULL
        DEFAULT 'ROUND_ROBIN',

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_assignment_config_category
        FOREIGN KEY (category_id)
        REFERENCES issue_categories(id)
);
Why next_sequence?

It stores the state used by the assignment engine. The implementation can use this value in a transaction to select the next eligible admin and update the cursor.

Concurrency note: The assignment cursor should be updated using a safe transaction strategy. The exact algorithm needs to account for active admin membership, category-specific ordering, and concurrent ticket creation.

6.6 Tickets

This is the core table.

CREATE TABLE tickets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    ticket_number VARCHAR(30) NOT NULL UNIQUE,

    employee_id BIGINT NOT NULL,

    category_id BIGINT NOT NULL,

    assigned_admin_mapping_id BIGINT NULL,

    title VARCHAR(200) NOT NULL,

    description TEXT NOT NULL,

    priority ENUM(
        'LOW',
        'MEDIUM',
        'HIGH',
        'CRITICAL'
    ) NOT NULL DEFAULT 'MEDIUM',

    status ENUM(
        'OPEN',
        'ASSIGNED',
        'IN_PROGRESS',
        'RESOLVED',
        'CLOSED',
        'REOPENED'
    ) NOT NULL DEFAULT 'OPEN',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    assigned_at TIMESTAMP NULL,

    resolved_at TIMESTAMP NULL,

    closed_at TIMESTAMP NULL,

    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tickets_employee
        FOREIGN KEY (employee_id)
        REFERENCES users(id),

    CONSTRAINT fk_tickets_category
        FOREIGN KEY (category_id)
        REFERENCES issue_categories(id),

    CONSTRAINT fk_tickets_admin_mapping
        FOREIGN KEY (assigned_admin_mapping_id)
        REFERENCES admin_category_assignments(id),

    INDEX idx_tickets_employee (employee_id),

    INDEX idx_tickets_category_status (category_id, status),

    INDEX idx_tickets_assigned_status (assigned_admin_mapping_id, status),

    INDEX idx_tickets_priority_status (priority, status)
);
Important correction for a production-ready design

A ticket's assigned admin mapping should remain auditable even if the admin is later deactivated or their category mapping changes. The ticket_assignment_history table below preserves the history. You may also add a direct assigned_admin_id reference on tickets if you want simplified queries, but the history table should remain the source of assignment events.

6.7 Ticket assignment history
CREATE TABLE ticket_assignment_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    ticket_id BIGINT NOT NULL,

    assigned_admin_id BIGINT NOT NULL,

    category_id BIGINT NOT NULL,

    assigned_by_user_id BIGINT NULL,

    assignment_method VARCHAR(30) NOT NULL,

    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    unassigned_at TIMESTAMP NULL,

    is_current BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_assignment_history_ticket
        FOREIGN KEY (ticket_id)
        REFERENCES tickets(id),

    CONSTRAINT fk_assignment_history_admin
        FOREIGN KEY (assigned_admin_id)
        REFERENCES users(id),

    CONSTRAINT fk_assignment_history_category
        FOREIGN KEY (category_id)
        REFERENCES issue_categories(id),

    CONSTRAINT fk_assignment_history_assigner
        FOREIGN KEY (assigned_by_user_id)
        REFERENCES users(id),

    INDEX idx_assignment_history_ticket (ticket_id),

    INDEX idx_assignment_history_admin (assigned_admin_id)
);
Example
Ticket TKT-1001
    ↓
Assigned to H001
    ↓
Admin unavailable
    ↓
Manager reassigns
    ↓
Assigned to H002

The history records both assignments, allowing the manager to trace the ticket's ownership over time.