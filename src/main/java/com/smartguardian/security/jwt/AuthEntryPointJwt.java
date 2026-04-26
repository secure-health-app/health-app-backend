package com.smartguardian.security.jwt;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;


/* ===================== AUTH ENTRY POINT ===================== */

// Returns JSON 401 responses when protected endpoints are accessed without valid authentication
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    // Triggered automatically by Spring Security after authentication failure
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        // Log failed access attempts for debugging/security monitoring
        logger.error(
                "Unauthorized error: {} - {} {}",
                authException.getMessage(),
                request.getMethod(),
                request.getRequestURI()
        );

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final ObjectMapper mapper = new ObjectMapper();

        mapper.writeValue(
                response.getOutputStream(),
                new com.smartguardian.payload.response.MessageResponse(
                        "Error: Unauthorized"
                )
        );
    }
}