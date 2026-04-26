package com.smartguardian.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartguardian.model.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


/* ===================== USER DETAILS ===================== */

// Spring Security user object used during login
public class UserDetailsImpl implements UserDetails {

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    public UserDetailsImpl(
            Long id,
            String username,
            String email,
            String password
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // create UserDetails from database user
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword()
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    // no roles used in this project
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // all accounts active in this project
    @Override
    public boolean isEnabled() {
        return true;
    }
}
