package com.securehealthapp.health_app_backend.payload.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}