package com.smartguardian.security.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartguardian.model.User;
import com.smartguardian.repository.UserRepository;
import com.smartguardian.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class FitbitApiService {

    private static final String FITBIT_API_BASE = "https://api.fitbit.com/1/user/-/";

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.client-secret}")
    private String clientSecret;

    public FitbitApiService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /* ===================== PUBLIC API ===================== */

    public String getHeartRateForDate(String date) {

        User user = getAuthenticatedUser();

        if (isTokenExpired(user)) {
            refreshAccessToken(user);
        }

        String url = FITBIT_API_BASE +
                "activities/heart/date/" + date + "/1d.json";

        return performGetRequest(url, user);
    }

    public String getStepsForDate(String date) {

        User user = getAuthenticatedUser();

        if (isTokenExpired(user)) {
            refreshAccessToken(user);
        }

        String url = FITBIT_API_BASE +
                "activities/date/" + date + ".json";

        return performGetRequest(url, user);
    }

    public String getSleepForDate(String date) {

        User user = getAuthenticatedUser();

        if (isTokenExpired(user)) {
            refreshAccessToken(user);
        }

        String url = FITBIT_API_BASE +
                "sleep/date/" + date + ".json";

        return performGetRequest(url, user);
    }


    /* ===================== CORE LOGIC ===================== */

    private boolean isTokenExpired(User user) {
        return user.getFitbitTokenExpiry() == null ||
                Instant.now().isAfter(user.getFitbitTokenExpiry());
    }

    private String performGetRequest(String url, User user) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getFitbitAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Fitbit API request failed");
        }

        return response.getBody();
    }

    /* ===================== TOKEN REFRESH ===================== */

    private void refreshAccessToken(User user) {

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
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", user.getFitbitRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        tokenUrl,
                        request,
                        String.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to refresh Fitbit token");
        }

        updateTokens(response.getBody(), user);
    }

    private void updateTokens(String responseBody, User user) {

        try {
            Map<String, Object> tokenData =
                    objectMapper.readValue(responseBody, Map.class);

            user.setFitbitAccessToken((String) tokenData.get("access_token"));
            user.setFitbitRefreshToken((String) tokenData.get("refresh_token"));
            user.setFitbitTokenExpiry(
                    Instant.now().plusSeconds(
                            ((Number) tokenData.get("expires_in")).longValue()
                    )
            );

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse refreshed Fitbit token", e);
        }
    }

    /* ===================== AUTH ===================== */

    private User getAuthenticatedUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl userDetails =
                (UserDetailsImpl) auth.getPrincipal();

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
