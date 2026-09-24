# PROJECT DEVELOPMENT PROMPT

## Project Title

Organization Ticket Management and Resolution System

---

# 1. ROLE AND RESPONSIBILITY

Act as a senior Java Spring Boot backend developer, software architect, database designer, and full-stack application engineer.

Your responsibility is to design and implement a production-oriented Organization Ticket Management System using:

* Backend: Java Spring Boot
* Database: MySQL
* Frontend: React.js
* API communication: REST APIs
* Authentication: Spring Security with JWT
* ORM: Spring Data JPA / Hibernate
* Build tool: Maven

The application will be used by an organization containing multiple employees, hardware admins, software admins, and managers.

The system must provide centralized ticket creation, automatic ticket assignment, issue resolution, employee confirmation, and organization-wide monitoring.

Follow clean architecture, proper separation of concerns, secure authorization, database integrity, validation, and maintainable coding practices.

---

# 2. MOST IMPORTANT DEVELOPMENT CONDITION: BACKEND FIRST

THIS IS A STRICT REQUIREMENT.

## PHASE 1: COMPLETE BACKEND FIRST

Start the project by developing ONLY the Spring Boot backend and MySQL database.

Do NOT start React frontend development until all required backend functionality has been implemented, tested, and verified.

During the backend phase:

* Do not create React components.
* Do not build frontend dashboards.
* Do not create frontend pages.
* Do not implement frontend routing.
* Do not integrate React with the backend.
* Do not skip backend testing in order to start frontend work.

Complete the backend independently using REST APIs, service logic, database operations, validation, authentication, authorization, and automated tests.

The backend must be usable through Postman, Swagger/OpenAPI, or another API testing tool before frontend development begins.

## Backend completion criteria

Before moving to frontend development, verify:

1. Project builds successfully.
2. MySQL connection works.
3. Database tables and relationships are created correctly.
4. Authentication works.
5. Role-based authorization works.
6. Employee ticket creation works.
7. Hardware and software category selection works.
8. Automatic round-robin admin assignment works.
9. Admin ticket management works.
10. Resolution submission works.
11. Employee confirmation and rejection work.
12. Reopened ticket workflow works.
13. Manager can view all organization tickets.
14. Manager can manage admin accounts and category assignments.
15. Priority filtering and ticket status filtering work.
16. Validation and exception handling work.
17. Concurrent ticket assignment is handled safely.
18. Backend tests pass.
19. API documentation is available.
20. The complete backend is verified through API testing.

ONLY AFTER THESE REQUIREMENTS ARE COMPLETED AND VERIFIED SHOULD YOU START PHASE 2: REACT FRONTEND DEVELOPMENT.

If any backend requirement is incomplete, continue working on the backend rather than moving to frontend.

---

# 3. PROJECT OBJECTIVE

Build a centralized organization ticket management system where employees can report problems, the system automatically assigns tickets to suitable admins, admins resolve the issues, and managers monitor all ticket activities.

The organization will have three main user roles:

1. Employee
2. Admin
3. Manager

The Admin role will have two issue categories:

* Hardware Admin
* Software Admin

When an employee raises a ticket, the employee must select the issue category.

If the employee selects Hardware, the ticket must be assigned only to an eligible Hardware Admin.

If the employee selects Software, the ticket must be assigned only to an eligible Software Admin.

The assignment must follow a round-robin strategy, ensuring sequential distribution among eligible active admins.

The manager must be able to configure and manage admin accounts, monitor all organization tickets, and view complete ticket resolution details.

---

# 4. USER ROLES AND RESPONSIBILITIES

## 4.1 EMPLOYEE

Employees are users who raise tickets regarding organizational problems.

Employee features:

* Login through the Employee portal.
* Create new tickets.
* Select issue category.
* Enter issue title.
* Enter detailed issue description.
* Select ticket priority.
* View own tickets.
* View ticket status.
* View assigned admin information permitted by the system.
* View resolution details.
* Confirm whether the issue is solved.
* Reject the resolution if the issue is not solved.
* Reopen the ticket if the problem persists.
* View ticket history and updates.

Employee restrictions:

* Employee can view only their own tickets.
* Employee cannot view other employees' tickets.
* Employee cannot assign tickets manually.
* Employee cannot change ticket ownership.
* Employee cannot access manager-only APIs.
* Employee cannot access unrelated admin tickets.

---

## 4.2 ADMIN

Admins are responsible for resolving tickets assigned to them.

There are two types of admins:

1. Hardware Admin
2. Software Admin

An admin must be assigned to one or more supported issue categories through the admin-category mapping.

### Hardware Admin responsibilities

* Login through the Hardware Admin portal.
* View assigned hardware tickets.
* View permitted employee information.
* View issue title and description.
* View ticket priority.
* Update ticket status.
* Add comments and investigation notes.
* Work on the issue.
* Submit resolution details.
* Mark the ticket as resolved.
* View reopened tickets assigned to them.
* Continue working on reopened tickets.

### Software Admin responsibilities

The Software Admin must have the same capabilities as the Hardware Admin, but software ticket assignments must be restricted to eligible software admins.

Admin restrictions:

* Admin cannot access all organization tickets by default.
* Admin cannot access manager-only functionality.
* Admin cannot modify another admin's tickets without authorization.
* Admin cannot close tickets on behalf of employees.
* Admin cannot bypass issue category restrictions.
* Admin cannot mark a ticket as resolved without providing required resolution details.
* Admin cannot assign tickets to an ineligible admin through a manipulated frontend request.

The backend must validate all admin permissions.

---

## 4.3 MANAGER

The manager is responsible for organization-wide monitoring and administration.

Manager features:

### Admin management

* Create admin accounts.
* View admin accounts.
* Update admin details where authorized.
* Activate admin accounts.
* Deactivate admin accounts.
* Assign Hardware category to admins.
* Assign Software category to admins.
* View the number of active hardware admins.
* View the number of active software admins.
* View admin workload.
* Configure and manage assignment settings.
* Reassign existing tickets through an authorized workflow.
* Ensure inactive admins do not receive new tickets.

### Ticket monitoring

The manager must be able to view ALL organization tickets.

The manager must be able to see:

* Ticket number.
* Employee name.
* Employee ID.
* Employee contact number, subject to data-access policy.
* Issue category.
* Issue title.
* Issue description.
* Priority.
* Ticket status.
* Assigned admin name.
* Assigned admin employee ID.
* Assignment date.
* Assignment method.
* Resolution details.
* Resolution date.
* Employee confirmation status.
* Reopening information.
* Ticket status history.
* Assignment history.

### Manager dashboard

Display:

* Total number of tickets.
* Open tickets.
* Assigned tickets.
* In-progress tickets.
* Resolved tickets.
* Tickets awaiting employee confirmation.
* Closed tickets.
* Reopened tickets.
* Tickets by Hardware and Software category.
* Tickets by priority.
* Admin workload summary.
* Tickets with no available admin.
* Recent ticket activities.

The manager has organization-wide visibility, but sensitive personal data must be returned only where necessary and authorized.

---

# 5. LOGIN AND AUTHENTICATION REQUIREMENTS

The frontend will eventually provide four role-selection portals:

1. Employee
2. Hardware Admin
3. Software Admin
4. Manager

The role-selection buttons are for directing users to the relevant login experience.

IMPORTANT SECURITY REQUIREMENT:

Selecting a login button must not grant the selected role.

The backend must authenticate the actual user account and validate its role and permissions.

For example:

* A user selecting Hardware Admin must not gain admin privileges unless their authenticated account has the Admin role and appropriate category authorization.
* A regular employee must not access manager APIs by selecting the Manager button.
* A Software Admin must not access unauthorized hardware operations.

Implement:

* Spring Security.
* JWT-based authentication.
* Secure password hashing using BCrypt or an appropriate password encoder.
* Authentication endpoint.
* JWT validation.
* Role-based authorization.
* Proper unauthorized and forbidden responses.
* Secure token handling.
* Account active/inactive validation.

Suggested roles:

* ROLE_EMPLOYEE
* ROLE_ADMIN
* ROLE_MANAGER

Admin category must be handled separately from the general ADMIN role.

Use authorization checks on protected backend endpoints.

---

# 6. ISSUE CATEGORIES

The initial system must support two issue categories:

1. HARDWARE
2. SOFTWARE

Examples of hardware issues:

* Laptop not powering on.
* Keyboard not working.
* Mouse not working.
* Monitor failure.
* Printer problem.
* Hardware replacement request.

Examples of software issues:

* Application not opening.
* Software installation problem.
* Login issue in an application.
* Application error.
* Configuration issue.
* Software access problem.

The issue category must be stored in the database.

The category selected by the employee must be validated on the backend.

The category must determine the eligible admin assignment pool.

---

# 7. TICKET CREATION REQUIREMENTS

Employees must be able to raise a ticket.

## Ticket form

Required fields:

* Issue category.
* Ticket title.
* Detailed description.
* Priority.

System-generated fields:

* Ticket ID.
* Unique ticket number.
* Employee ID.
* Employee account reference.
* Creation timestamp.
* Initial status.
* Assignment information.
* Updated timestamp.

Employee information must be retrieved from the authenticated user account instead of trusting employee-provided identity fields.

The system must validate:

* Title is not blank.
* Description is not blank.
* Category is valid and active.
* Priority is valid.
* Employee account is active.
* Request is authorized.
* Ticket number is unique.

When the ticket is successfully created:

1. Save the ticket.
2. Determine the eligible admin pool based on the category.
3. Assign the ticket using the round-robin strategy if an eligible admin exists.
4. Record assignment details.
5. Update the ticket status to ASSIGNED.
6. Store the assignment timestamp.
7. Record the assignment event in the assignment history.
8. Return the created ticket response.

If no eligible admin is available, do not silently assign the ticket to an incorrect category or inactive admin.

Handle the unassigned case with a clearly defined backend behavior, such as keeping the ticket OPEN with an assignment-pending reason, and make it visible to the manager.

---

# 8. ROUND-ROBIN ASSIGNMENT SYSTEM

This is one of the main business features of the application.

The system must maintain two separate assignment pools:

* Hardware Admin Pool.
* Software Admin Pool.

When an employee creates a Hardware ticket, only eligible active Hardware Admins may be selected.

When an employee creates a Software ticket, only eligible active Software Admins may be selected.

## Example

Manager configures:

Hardware Admins:

* H001
* H002
* H003

Software Admins:

* S001
* S002

Hardware ticket sequence:

Ticket 1 → H001
Ticket 2 → H002
Ticket 3 → H003
Ticket 4 → H001
Ticket 5 → H002

Software ticket sequence:

Ticket 1 → S001
Ticket 2 → S002
Ticket 3 → S001
Ticket 4 → S002

The round-robin sequence must be independent for each issue category.

## Assignment requirements

* Use active eligible admins only.
* Do not assign Hardware tickets to Software-only admins.
* Do not assign Software tickets to Hardware-only admins.
* Do not assign new tickets to inactive admins.
* Preserve existing ticket assignments.
* Record the selected admin.
* Record assignment date.
* Record assignment method.
* Support manager-authorized reassignment.
* Maintain assignment history.

## Concurrency requirement

The system must safely handle multiple employees raising tickets at the same time.

Do not use an unsafe read-then-update process that can cause duplicate assignment cursor selection.

Use appropriate Spring transaction management and MySQL locking or another safe concurrency strategy.

The assignment process must:

1. Acquire the relevant category assignment state safely.
2. Identify eligible active admins in a deterministic order.
3. Select the next admin according to round-robin rules.
4. Persist the ticket assignment.
5. Update the assignment cursor.
6. Record the assignment event.
7. Commit the transaction.

If an assignment operation fails, ensure that the database does not leave inconsistent ticket and assignment state.

---

# 9. TICKET STATUS WORKFLOW

Use the following statuses:

* OPEN
* ASSIGNED
* IN_PROGRESS
* RESOLVED
* CLOSED
* REOPENED

## Workflow

Employee creates ticket:

OPEN

System assigns admin:

ASSIGNED

Admin starts working:

IN_PROGRESS

Admin submits solution:

RESOLVED

Employee confirms issue is solved:

CLOSED

Employee says issue is not solved:

REOPENED

Admin resumes work:

IN_PROGRESS

Admin submits a new solution:

RESOLVED

Employee confirms:

CLOSED

## Status rules

* A ticket must not be marked CLOSED solely by an admin.
* An employee can confirm or reject a resolution for their own ticket.
* Only authorized users can update status.
* Status transitions must be validated by the backend.
* Every status transition must be stored in a history table.
* A ticket cannot be modified as if it were active after closure without an authorized workflow.
* Reopened tickets must remain linked to their original ticket number.
* Reopened tickets must preserve previous resolution and assignment history.

The backend must prevent invalid status transitions.

---

# 10. ADMIN RESOLUTION SYSTEM

When an admin finishes working on a ticket, they must submit resolution details.

Required resolution information:

* Ticket ID.
* Admin who resolved the ticket.
* Solution description.
* Resolution timestamp.
* Resolution status.

The admin must not be able to submit a resolution for a ticket they are not authorized to manage.

When the admin submits a resolution:

1. Validate ticket ownership or authorized assignment.
2. Validate required resolution details.
3. Save the resolution.
4. Update ticket status to RESOLVED.
5. Set the resolution timestamp.
6. Record the status change.
7. Return the updated ticket details.

The ticket remains pending employee confirmation.

---

# 11. EMPLOYEE CONFIRMATION AND REOPENING

This is a mandatory feature.

After the admin marks a ticket as RESOLVED, the employee must be able to review the solution.

Employee actions:

1. Confirm problem solved.
2. Reject resolution because the issue is not solved.

## If employee confirms

* Save confirmation details.
* Record confirmation timestamp.
* Update ticket status to CLOSED.
* Store confirmation decision.
* Record the status change.
* Preserve the resolution record.

## If employee rejects

* Save rejection details.
* Record rejection timestamp.
* Update ticket status to REOPENED.
* Preserve the previous resolution.
* Add a reason for rejection.
* Return the ticket to the admin workflow.

The system must ensure that only the ticket's owning employee can confirm or reject its resolution, unless an explicitly authorized alternative workflow is introduced.

---

# 12. PRIORITY MANAGEMENT

The system must support four ticket priorities:

* LOW
* MEDIUM
* HIGH
* CRITICAL

The priority must be stored in the database.

Employees may select a priority, but the backend must validate the submitted value.

The manager must be able to filter tickets by priority.

The admin must be able to see the priority of assigned tickets.

The manager dashboard must display priority-based statistics.

Optional future functionality:

* Priority escalation.
* SLA deadline calculation.
* Critical ticket notifications.
* Manager approval for critical priority.
* Automatic escalation of overdue tickets.

Do not implement advanced SLA automation unless it is explicitly included in the current development scope.

---

# 13. DATABASE DESIGN

Use MySQL with proper relational design.

Create the following tables:

1. roles
2. users
3. issue_categories
4. admin_category_assignments
5. assignment_configs
6. tickets
7. ticket_assignment_history
8. ticket_status_history
9. ticket_resolutions
10. ticket_confirmations
11. ticket_comments

Use foreign keys, unique constraints, indexes, and suitable data types.

## USERS

Fields:

* id
* employee_id
* full_name
* email
* password_hash
* contact_number
* role_id
* is_active
* created_at
* updated_at

## ROLES

Fields:

* id
* name

Initial roles:

* EMPLOYEE
* ADMIN
* MANAGER

## ISSUE_CATEGORIES

Fields:

* id
* name
* description
* is_active

Initial categories:

* HARDWARE
* SOFTWARE

## ADMIN_CATEGORY_ASSIGNMENTS

Fields:

* id
* admin_id
* category_id
* is_active
* created_at

Rules:

* Admin must have ADMIN role.
* Admin-category relationship must be unique.
* Only active eligible mappings participate in new assignment selection.
* Category eligibility must be checked in backend logic.

## ASSIGNMENT_CONFIGS

Fields:

* id
* category_id
* next_sequence
* assignment_strategy
* updated_at

Rules:

* One assignment configuration per category.
* Assignment strategy initially ROUND_ROBIN.
* Assignment state must be updated safely under concurrency.

## TICKETS

Fields:

* id
* ticket_number
* employee_id
* category_id
* assigned_admin_mapping_id or a suitable assignment reference
* title
* description
* priority
* status
* created_at
* assigned_at
* resolved_at
* closed_at
* updated_at

Rules:

* Ticket number unique.
* Employee must exist.
* Category must exist.
* Assignment must be valid.
* Ticket status must be controlled.
* Priority must be controlled.

## TICKET_ASSIGNMENT_HISTORY

Fields:

* id
* ticket_id
* assigned_admin_id
* category_id
* assigned_by_user_id
* assignment_method
* assigned_at
* unassigned_at
* is_current

This table must preserve the ticket's assignment history.

## TICKET_STATUS_HISTORY

Fields:

* id
* ticket_id
* previous_status
* new_status
* changed_by_user_id
* change_reason
* changed_at

Record every status transition.

## TICKET_RESOLUTIONS

Fields:

* id
* ticket_id
* resolved_by
* solution_description
* resolved_at

A ticket can have multiple resolution records over its lifecycle if it is reopened and resolved again, or use a versioned resolution approach.

## TICKET_CONFIRMATIONS

Fields:

* id
* ticket_id
* employee_id
* decision
* reason
* confirmed_at

Decision values:

* CONFIRMED
* REJECTED

## TICKET_COMMENTS

Fields:

* id
* ticket_id
* user_id
* comment_text
* created_at
* updated_at

Use appropriate authorization rules for viewing and adding comments.

---

# 14. BACKEND ARCHITECTURE

Use a layered Spring Boot architecture.

Suggested package structure:

backend/
└── src/main/java/com/example/ticket/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── mapper/
├── security/
├── exception/
├── config/
└── enums/

## Responsibilities

### Controller

* Receive HTTP requests.
* Validate request format.
* Call service layer.
* Return appropriate HTTP responses.
* Avoid placing business logic in controllers.

### Service

* Implement business logic.
* Validate role permissions.
* Handle ticket creation.
* Handle assignment logic.
* Handle status transitions.
* Handle resolution workflow.
* Handle manager administration.
* Define transaction boundaries.

### Repository

* Implement database access.
* Use Spring Data JPA.
* Add custom queries where required.
* Support category-based ticket retrieval.
* Support manager ticket filtering.
* Support safe assignment-state retrieval.

### DTO

Use request and response DTOs instead of exposing JPA entities directly.

Suggested DTOs:

* LoginRequest
* LoginResponse
* CreateTicketRequest
* TicketResponse
* UpdateTicketStatusRequest
* SubmitResolutionRequest
* TicketConfirmationRequest
* AddCommentRequest
* CreateAdminRequest
* UpdateAdminCategoryRequest
* ManagerTicketFilterRequest

---

# 15. SUGGESTED REST API ENDPOINTS

Implement secure REST APIs.

## Authentication

POST /api/auth/login

POST /api/auth/register (only if registration is part of the chosen account workflow)

GET /api/auth/me

## Employee APIs

POST /api/employee/tickets

GET /api/employee/tickets

GET /api/employee/tickets/{ticketId}

POST /api/employee/tickets/{ticketId}/confirmation

POST /api/employee/tickets/{ticketId}/rejection (or a unified confirmation endpoint)

## Admin APIs

GET /api/admin/tickets

GET /api/admin/tickets/{ticketId}

PATCH /api/admin/tickets/{ticketId}/status

POST /api/admin/tickets/{ticketId}/resolution

POST /api/admin/tickets/{ticketId}/comments

GET /api/admin/tickets/{ticketId}/history

## Manager APIs

GET /api/manager/tickets

GET /api/manager/tickets/{ticketId}

GET /api/manager/dashboard

POST /api/manager/admins

GET /api/manager/admins

PATCH /api/manager/admins/{adminId}

POST /api/manager/admins/{adminId}/categories

DELETE /api/manager/admins/{adminId}/categories/{categoryId}

GET /api/manager/assignment-configs

PATCH /api/manager/assignment-configs/{categoryId}

POST /api/manager/tickets/{ticketId}/reassign

GET /api/manager/tickets/{ticketId}/history

These endpoint names are proposed. Apply consistent naming and authorization rules.

Do not expose manager endpoints to unauthorized users.

---

# 16. VALIDATION AND ERROR HANDLING

Implement global exception handling using @RestControllerAdvice.

Handle:

* Validation errors.
* Authentication failures.
* Authorization failures.
* Resource not found.
* Invalid ticket status transitions.
* Duplicate employee ID.
* Duplicate email.
* Duplicate ticket number.
* Invalid issue category.
* Inactive admin.
* No eligible admin.
* Invalid resolution submission.
* Unauthorized employee confirmation.
* Database constraint violations.
* Concurrent assignment conflicts.

Return a consistent error response format.

Example:

{
"timestamp": "2026-09-24T10:30:00",
"status": 400,
"error": "VALIDATION_ERROR",
"message": "Ticket title is required",
"path": "/api/employee/tickets"
}

Do not expose stack traces, passwords, tokens, or sensitive internal details in API responses.

---

# 17. TESTING REQUIREMENTS

Before frontend development, test the complete backend.

## Unit tests

Test:

* Ticket creation.
* Category validation.
* Round-robin assignment.
* Admin eligibility.
* Status transitions.
* Resolution submission.
* Employee confirmation.
* Employee rejection.
* Manager authorization.
* Admin authorization.

## Integration tests

Test:

* MySQL database integration.
* Repository queries.
* Ticket creation and assignment transaction.
* Status history persistence.
* Resolution persistence.
* Manager ticket retrieval.
* Admin category mapping.

## Security tests

Test:

* Employee cannot access manager APIs.
* Admin cannot access unauthorized manager APIs.
* Software admin cannot receive unauthorized category tickets.
* Employee cannot confirm another employee's ticket.
* Inactive users cannot perform protected operations.
* Unauthenticated requests are rejected.

## Concurrency tests

Test:

* Multiple employees create tickets concurrently.
* Assignment cursor updates remain consistent.
* Tickets are assigned to eligible admins.
* No invalid assignment state is produced.
* Database transaction behavior is correct.

Use JUnit 5, Mockito, Spring Boot Test, and appropriate integration test support.

---

# 18. API DOCUMENTATION

Implement Swagger/OpenAPI documentation using springdoc-openapi or an appropriate solution.

Document:

* Authentication APIs.
* Employee APIs.
* Admin APIs.
* Manager APIs.
* Request bodies.
* Response formats.
* Error responses.
* Authentication requirements.
* Role restrictions.

The backend must be understandable and testable independently of the frontend.

---

# 19. BACKEND DEVELOPMENT ORDER

Follow this exact implementation order.

### Step 1: Project setup

* Create Spring Boot project.
* Configure Maven.
* Configure Java version.
* Add required dependencies.
* Configure MySQL.
* Configure application profiles.

### Step 2: Database and entities

* Create entities.
* Create enums.
* Define relationships.
* Add constraints.
* Configure JPA mappings.
* Set up migrations or schema management.

### Step 3: Authentication and security

* Implement users and roles.
* Implement password hashing.
* Implement login.
* Implement JWT authentication.
* Implement role-based authorization.

### Step 4: Employee ticket creation

* Create ticket DTOs.
* Create ticket repository.
* Implement ticket service.
* Validate category and priority.
* Persist employee ticket.
* Implement ticket retrieval.

### Step 5: Admin assignment

* Implement admin-category mapping.
* Implement assignment configuration.
* Implement round-robin algorithm.
* Implement transaction safety.
* Implement assignment history.

### Step 6: Admin workflow

* Assigned ticket retrieval.
* Status update.
* Comments.
* Resolution submission.
* Status history.

### Step 7: Employee confirmation

* Confirmation endpoint.
* Rejection endpoint or unified decision endpoint.
* Closed workflow.
* Reopened workflow.

### Step 8: Manager workflow

* Admin management.
* Category mapping management.
* Assignment configuration.
* Organization-wide ticket listing.
* Ticket filtering.
* Dashboard metrics.
* Reassignment workflow.

### Step 9: Validation and testing

* Global exceptions.
* Unit tests.
* Integration tests.
* Security tests.
* Concurrency tests.

### Step 10: Backend verification

* Run the application.
* Test APIs.
* Verify database persistence.
* Verify authorization.
* Verify assignment logic.
* Verify all status transitions.
* Verify manager dashboard data.
* Fix all critical defects.

DO NOT MOVE TO FRONTEND UNTIL THIS BACKEND PHASE IS COMPLETE.

---

# 20. FRONTEND DEVELOPMENT: ONLY AFTER BACKEND COMPLETION

Once the entire backend has been implemented, tested, and verified, start the React frontend.

Use React.js.

Frontend development must consume the completed backend REST APIs rather than implementing duplicate business logic.

## Frontend pages

### Login

* Employee login portal.
* Hardware Admin login portal.
* Software Admin login portal.
* Manager login portal.

### Employee dashboard

* Create ticket.
* My tickets.
* Ticket details.
* Resolution details.
* Confirm solved.
* Reject resolution.
* Reopened ticket information.

### Hardware Admin dashboard

* Assigned hardware tickets.
* Ticket details.
* Status update.
* Resolution form.
* Comments.
* Reopened tickets.

### Software Admin dashboard

Same as Hardware Admin dashboard with software-specific assignment and access.

### Manager dashboard

* All tickets.
* Ticket filters.
* Admin management.
* Category management.
* Assignment configuration.
* Workload statistics.
* Ticket history.
* Resolution monitoring.

## Frontend security

* Use authenticated API requests.
* Handle expired tokens.
* Do not rely on frontend-only role restrictions.
* Do not expose unauthorized actions through UI controls.
* Ensure backend authorization remains the source of truth.

---

# 21. CODING STANDARDS

Follow these rules throughout development:

* Use clean, readable Java code.
* Follow SOLID principles where appropriate.
* Use DTOs for API boundaries.
* Avoid business logic in controllers.
* Use meaningful class and method names.
* Use constructor injection.
* Use transactions where necessary.
* Validate all user inputs.
* Do not hardcode admin IDs.
* Do not hardcode assignment sequences.
* Avoid unnecessary duplication.
* Use consistent API response formats.
* Add useful comments only where needed.
* Do not silently ignore exceptions.
* Never store plaintext passwords.
* Do not commit secrets to source control.
* Do not implement fake success responses.
* Do not claim features are completed without testing them.

---

# 22. RESPONSE FORMAT DURING DEVELOPMENT

Follow this format whenever you complete a development step.

## Step completed

Explain what was implemented.

## Files created or modified

List all files and their purposes.

## Implementation explanation

Explain the business logic and technical design.

## Code

Provide complete code for the requested implementation.

## Database changes

Mention new tables, columns, constraints, indexes, or migrations.

## API details

Explain endpoints, request bodies, responses, and authorization.

## Testing

Mention tests created and their results. Do not claim tests passed unless they were actually executed.

## Next step

Suggest the next backend task in the defined development order.

IMPORTANT:

* Do not skip implementation steps.
* Do not move to frontend before backend completion.
* If requirements are ambiguous, identify the ambiguity and make a documented, reasonable assumption or ask for clarification.
* If an implementation has limitations, explain them honestly.
* Maintain consistency with all previously implemented features.
* Do not remove existing working features without explicit instruction.

---

# FINAL INSTRUCTION

Build this Organization Ticket Management System systematically.

FIRST: Complete the entire Spring Boot backend and MySQL database implementation.

SECOND: Test and verify every backend requirement, including security, ticket creation, category-specific round-robin assignment, resolution workflow, employee confirmation, reopening, manager visibility, and data integrity.

THIRD: Only after the backend is fully implemented and verified, begin React frontend development.

Do not start frontend development early.

Start by explaining the backend architecture, confirming the development plan, and implementing the first backend step: project setup and database configuration.

Then proceed step by step until the complete backend is finished.
