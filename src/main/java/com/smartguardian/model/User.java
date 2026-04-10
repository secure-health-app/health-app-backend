package com.smartguardian.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


/* ===================== USER ENTITY ===================== */

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Data
@NoArgsConstructor
public class User {


    /* ===================== PRIMARY KEY ===================== */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /* ===================== LOGIN FIELDS ===================== */

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;


    /* ===================== PROFILE ===================== */

    @NotBlank
    @Size(max = 100)
    private String name;

    private String phoneNumber;


    /* ===================== FITBIT TOKENS ===================== */

    // Fitbit access token
    @Column(length = 2048, nullable = true)
    private String fitbitAccessToken;

    // Fitbit refresh token
    @Column(length = 2048)
    private String fitbitRefreshToken;

    // expiry time for access token
    private Instant fitbitTokenExpiry;

    // Fitbit user id
    private String fitbitUserId;

    // the username of the caregiver who monitors this user
    @Column(nullable = true)
    private String caregiverUsername;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
