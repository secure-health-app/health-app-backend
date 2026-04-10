package com.smartguardian.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.smartguardian.model.User;
import com.smartguardian.payload.request.LoginRequest;
import com.smartguardian.payload.request.SignupRequest;
import com.smartguardian.payload.response.JwtResponse;
import com.smartguardian.payload.response.MessageResponse;
import com.smartguardian.repository.UserRepository;
import com.smartguardian.security.jwt.JwtUtils;
import com.smartguardian.security.services.UserDetailsImpl;


/* ===================== AUTH CONTROLLER ===================== */

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    /* ===================== DEPENDENCIES ===================== */

    // handles login authentication
    @Autowired
    AuthenticationManager authenticationManager;

    // access user database
    @Autowired
    UserRepository userRepository;

    // password hashing
    @Autowired
    PasswordEncoder encoder;

    // generate JWT tokens
    @Autowired
    JwtUtils jwtUtils;


    /* ===================== SIGN IN ===================== */

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        // authenticate user credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // store authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // get logged in user details
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        // return token + user info
        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail()
                )
        );
    }


    /* ===================== SIGN UP ===================== */

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody SignupRequest signUpRequest) {

        // check if email already exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // create new user
        User user = new User(
                signUpRequest.getEmail(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword())
        );

        // optional phone number
        user.setPhoneNumber(signUpRequest.getPhoneNumber());

        // save user
        userRepository.save(user);

        return ResponseEntity.ok(
                new MessageResponse("User registered successfully!")
        );
    }

}