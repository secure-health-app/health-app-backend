package com.smartguardian.controller;

import com.smartguardian.security.services.FitbitAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth/fitbit")
public class FitbitAuthController {

    private final FitbitAuthService fitbitAuthService;

    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.redirect-uri}")
    private String redirectUri;

    public FitbitAuthController(FitbitAuthService fitbitAuthService) {
        this.fitbitAuthService = fitbitAuthService;
    }

    /**
     * Step 1: Redirects the authenticated user to Fitbit OAuth login.
     */
    @GetMapping("/connect")
    public void connectToFitbit(
            @RequestParam String token,
            HttpServletResponse response
    ) throws IOException {

        String scopes = "activity heartrate sleep";

        String authUrl =
                "https://www.fitbit.com/oauth2/authorize" +
                        "?response_type=code" +
                        "&client_id=" + clientId +
                        "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                        "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8) +
                        "&state=" + token;

        response.sendRedirect(authUrl);
    }

    /**
     * Step 2: Fitbit redirects here with an authorization code.
     * The code is exchanged for tokens in the service layer.
     */
    @GetMapping("/callback")
    public void fitbitCallback(
            String code,
            String state,
            HttpServletResponse response
    ) throws IOException {

        fitbitAuthService.exchangeCodeAndSaveTokens(code, state);

        response.sendRedirect(
                "http://localhost:5173/dashboard?fitbit=connected"
        );
    }
}
