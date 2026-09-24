package com.ticketmgmt.controller;

import com.ticketmgmt.dto.request.LoginRequest;
import com.ticketmgmt.dto.response.LoginResponse;
import com.ticketmgmt.entity.User;
import com.ticketmgmt.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Authentication", description = "Login and user profile APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "Authenticate and receive a JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        User user = authService.getCurrentUserProfile();
        return ResponseEntity.ok(Map.of(
                "id",            user.getId(),
                "employeeId",    user.getEmployeeId(),
                "fullName",      user.getFullName(),
                "email",         user.getEmail(),
                "role",          user.getRole().getName(),
                "isActive",      user.getIsActive(),
                "contactNumber", user.getContactNumber() != null ? user.getContactNumber() : "",
                "businessUnit",  user.getBusinessUnit() != null ? user.getBusinessUnit() : "",
                "workLocation",  user.getWorkLocation() != null ? user.getWorkLocation() : "",
                "assetTag",      user.getAssetTag() != null ? user.getAssetTag() : ""
        ));
    }

    @Operation(summary = "Register Employee", description = "Register a new employee account")
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody com.ticketmgmt.dto.request.RegisterEmployeeRequest request) {
        return ResponseEntity.ok(authService.registerEmployee(request));
    }
}
