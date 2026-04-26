package com.smartguardian.security.services;

import com.smartguardian.model.User;
import com.smartguardian.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/* ===================== USER DETAILS SERVICE ===================== */

// finds users in database during login
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;


    /* ===================== LOAD USER ===================== */

    // load user by email for Spring Security
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User Not Found with email: " + email)
                        );

        return UserDetailsImpl.build(user);
    }
}
