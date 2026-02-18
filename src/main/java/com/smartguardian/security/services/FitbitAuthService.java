package com.smartguardian.security.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartguardian.model.User;
import com.smartguardian.repository.UserRepository;
import com.smartguardian.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class FitbitAuthService {

    @Autowired
    private JwtUtils jwtUtils;

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.client-secret}")
    private String clientSecret;

    @Value("${fitbit.redirect-uri}")
    private String redirectUri;

    public FitbitAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Exchanges the OAuth authorization code for Fitbit access & refresh tokens
     * and securely stores them against the authenticated user.
     */
    public void exchangeCodeAndSaveTokens(String code, String state) {

        // Validate JWT from state
        if (!jwtUtils.validateJwtToken(state)) {
            throw new RuntimeException("Invalid JWT in OAuth state");
        }

        String username = jwtUtils.getUserNameFromJwtToken(state);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));


        String tokenUrl = "https://api.fitbit.com/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(
                HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString(
                        (clientId + ":" + clientSecret)
                                .getBytes(StandardCharsets.UTF_8)
                )
        );

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri.trim());
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(tokenUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to exchange Fitbit authorization code");
        }

        saveTokens(response.getBody(), user);
        System.out.println("JWT username/email from state: " + username);

    }

    /**
     * Parses Fitbit token response and persists values securely.
     */
    private void saveTokens(String responseBody, User user) {

        try {
            Map<String, Object> tokenData =
                    objectMapper.readValue(responseBody, Map.class);

            String accessToken = (String) tokenData.get("access_token");
            String refreshToken = (String) tokenData.get("refresh_token");
            String fitbitUserId = (String) tokenData.get("user_id");
            Integer expiresIn = (Integer) tokenData.get("expires_in");

            if (accessToken == null || refreshToken == null || expiresIn == null) {
                throw new RuntimeException("Invalid Fitbit token response");
            }

            user.setFitbitAccessToken(accessToken);
            user.setFitbitRefreshToken(refreshToken);
            user.setFitbitUserId(fitbitUserId);
            user.setFitbitTokenExpiry(
                    Instant.now().plusSeconds(expiresIn)
            );

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Fitbit token response", e);
        }
    }
}
