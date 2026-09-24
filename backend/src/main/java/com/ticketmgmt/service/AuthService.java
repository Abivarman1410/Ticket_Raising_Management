package com.ticketmgmt.service;

import com.ticketmgmt.dto.request.LoginRequest;
import com.ticketmgmt.dto.response.LoginResponse;
import com.ticketmgmt.entity.User;
import com.ticketmgmt.exception.ResourceNotFoundException;
import com.ticketmgmt.repository.UserRepository;
import com.ticketmgmt.security.JwtTokenProvider;
import com.ticketmgmt.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;
    private final UserRepository        userRepository;
    private final com.ticketmgmt.repository.RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Authenticate the user by email/password and return a JWT token.
     * Spring Security will throw BadCredentialsException or DisabledException on failure.
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Fetch full user to return complete profile info
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .employeeId(user.getEmployeeId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }

    /**
     * Return the currently authenticated user's profile.
     */
    public User getCurrentUserProfile() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Register a new employee.
     */
    public LoginResponse registerEmployee(com.ticketmgmt.dto.request.RegisterEmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new com.ticketmgmt.exception.BadRequestException("Email is already taken");
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new com.ticketmgmt.exception.BadRequestException("Employee ID is already taken");
        }

        com.ticketmgmt.entity.Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User newUser = User.builder()
                .employeeId(request.getEmployeeId())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .contactNumber(request.getContactNumber())
                .assetTag(request.getAssetTag())
                .businessUnit(request.getBusinessUnit())
                .workLocation(request.getWorkLocation())
                .role(employeeRole)
                .build();

        userRepository.save(newUser);

        // Auto-login after registration
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(request.getEmail());
        loginRequest.setPassword(request.getPassword());
        return login(loginRequest);
    }
}
