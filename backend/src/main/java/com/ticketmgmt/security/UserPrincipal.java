package com.ticketmgmt.security;

import com.ticketmgmt.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter between the domain User entity and Spring Security's UserDetails.
 */
public class UserPrincipal implements UserDetails {

    @Getter
    private final Long id;

    @Getter
    private final String employeeId;

    private final String email;
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id         = user.getId();
        this.employeeId = user.getEmployeeId();
        this.email      = user.getEmail();
        this.password   = user.getPasswordHash();
        this.active     = Boolean.TRUE.equals(user.getIsActive());
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        // We use email as the principal username for authentication
        return email;
    }

    @Override
    public boolean isAccountNonExpired()    { return true; }

    @Override
    public boolean isAccountNonLocked()     { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return active; }
}
