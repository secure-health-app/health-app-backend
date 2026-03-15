package com.smartguardian.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "users",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @NotBlank
    @Size(max = 100)
    private String name;

    private String phoneNumber;

    // To persist fitbit tokens linked to the user
    @Column(length = 2048, nullable = true)
    private String fitbitAccessToken;

    @Column(length = 2048)
    private String fitbitRefreshToken;

    private Instant fitbitTokenExpiry;

    private String fitbitUserId;

    // the username of the caregiver who monitors this user
    // set by the user in their profile settings
    // used to route alerts to the right caregiver
    @Column(nullable = true)
    private String caregiverUsername;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
