package com.smartguardian.controller;

import com.smartguardian.security.services.FitbitAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


/* ===================== FITBIT AUTH CONTROLLER ===================== */

@RestController
@RequestMapping("/api/auth/fitbit")
public class FitbitAuthController {

    // Service
    private final FitbitAuthService fitbitAuthService;

    // Config
    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.redirect-uri}")
    private String redirectUri;

    @Value("${frontend.url}")
    private String frontendUrl;

    public FitbitAuthController(FitbitAuthService fitbitAuthService) {
        this.fitbitAuthService = fitbitAuthService;
    }


    /* ===================== CONNECT ===================== */

    // Step 1: Redirect user to Fitbit OAuth login
    @GetMapping("/connect")
    public void connectToFitbit(
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {

        // Minimum Fitbit permissions needed for dashboard metrics
        String scopes = "activity heartrate sleep";

        String authUrl =
                "https://www.fitbit.com/oauth2/authorize" +
                        "?response_type=code" +
                        "&client_id=" + clientId +
                        "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                        "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                        // Reuse logged-in JWT in OAuth state so callback can identify the user account
                        "&state=" + token;

        response.sendRedirect(authUrl);
    }


    /* ===================== CALLBACK ===================== */

    // Step 2: Fitbit redirects back with auth code
    @GetMapping("/callback")
    public void fitbitCallback(
            String code,
            String state,
            HttpServletResponse response
    ) throws IOException {

        // exchange code for tokens
        fitbitAuthService.exchangeCodeAndSaveTokens(code, state);

        // Return user to dashboard and trigger frontend refresh flow
        response.sendRedirect(
                frontendUrl + "/?fitbit=connected"
        );
    }
}
