package com.smartguardian.security.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartguardian.model.FitbitDailySummary;
import com.smartguardian.model.User;
import com.smartguardian.repository.FitbitDailySummaryRepository;
import com.smartguardian.repository.UserRepository;

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
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;


/* ===================== FITBIT API SERVICE ===================== */

@Service
public class FitbitApiService {

    private static final String FITBIT_API_BASE =
            "https://api.fitbit.com/1/user/-/";

    private final UserRepository userRepository;
    private final FitbitDailySummaryRepository summaryRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // config
    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.client-secret}")
    private String clientSecret;

    public FitbitApiService(
            UserRepository userRepository,
            FitbitDailySummaryRepository summaryRepository
    ) {
        this.userRepository = userRepository;
        this.summaryRepository = summaryRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }


    /* ===================== PUBLIC API ===================== */

    // get heart rate for a specific date
    public String getHeartRateForDate(String date) {

        User user = getAuthenticatedUser();

        if (isTokenExpired(user))
            refreshAccessToken(user);

        return performGetRequest(
                FITBIT_API_BASE + "activities/heart/date/" + date + "/1d.json",
                user
        );
    }

    // get steps for a specific date
    public String getStepsForDate(String date) {

        User user = getAuthenticatedUser();

        if (isTokenExpired(user))
            refreshAccessToken(user);

        return performGetRequest(
                FITBIT_API_BASE + "activities/date/" + date + ".json",
                user
        );
    }

    // get sleep data for a specific date
    public String getSleepForDate(String date) {

        User user = getAuthenticatedUser();

        if (isTokenExpired(user))
            refreshAccessToken(user);

        return performGetRequest(
                FITBIT_API_BASE + "sleep/date/" + date + ".json",
                user
        );
    }


    /* ===================== FETCH AND SAVE ===================== */

    // fetch Fitbit data and store it in database
    public FitbitDailySummary fetchAndSaveDailySummary(String date) {

        User user = getAuthenticatedUser();
        if (isTokenExpired(user))
            refreshAccessToken(user);

        LocalDate localDate = LocalDate.parse(date);

        // if already saved for this date, return existing record
        return summaryRepository
                .findByUserAndDate(user, localDate)
                .orElseGet(() -> {

                    Integer restingHR =
                            parseRestingHeartRate(getHeartRateForDate(date));

                    Integer steps =
                            parseSteps(getStepsForDate(date));

                    Integer sleepMins =
                            parseSleepMinutes(getSleepForDate(date));

                    FitbitDailySummary summary =
                            FitbitDailySummary.builder()
                                    .user(user)
                                    .date(localDate)
                                    .restingHeartRate(restingHR)
                                    .steps(steps)
                                    .sleepMinutes(sleepMins)
                                    .build();

                    return summaryRepository.save(summary);
                });
    }


    /* ===================== PARSERS ===================== */

    // extract resting HR from Fitbit JSON
    private Integer parseRestingHeartRate(String json) {

        try {
            JsonNode root =
                    objectMapper.readTree(json);

            JsonNode value =
                    root
                            .path("activities-heart")
                            .get(0)
                            .path("value")
                            .path("restingHeartRate");

            return value.isMissingNode()
                    ? null
                    : value.asInt();

        } catch (Exception e) {
            return null;
        }
    }

    // extract steps from Fitbit JSON
    private Integer parseSteps(String json) {

        try {

            JsonNode root =
                    objectMapper.readTree(json);

            return root
                    .path("summary")
                    .path("steps")
                    .asInt();

        } catch (Exception e) {
            return null;
        }
    }

    // extract sleep minutes from Fitbit JSON
    private Integer parseSleepMinutes(String json) {

        try {

            JsonNode root =
                    objectMapper.readTree(json);

            return root
                    .path("summary")
                    .path("totalMinutesAsleep")
                    .asInt();

        } catch (Exception e) {
            return null;
        }
    }


    /* ===================== CORE LOGIC ===================== */

    // check if Fitbit token expired
    private boolean isTokenExpired(User user) {

        return user.getFitbitTokenExpiry() == null
                || Instant.now().isAfter(user.getFitbitTokenExpiry());
    }

    // generic GET request to Fitbit API
    private String performGetRequest(String url, User user) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(user.getFitbitAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Fitbit API request failed");
        }

        return response.getBody();
    }


    /* ===================== TOKEN REFRESH ===================== */

    // refresh expired Fitbit token
    private void refreshAccessToken(User user) {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED);

        headers.set(
                HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder()
                        .encodeToString(
                                (clientId + ":" + clientSecret)
                                        .getBytes(StandardCharsets.UTF_8)
                        )
        );

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("grant_type", "refresh_token");
        body.add("refresh_token", user.getFitbitRefreshToken());

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "https://api.fitbit.com/oauth2/token",
                        new HttpEntity<>(body, headers),
                        String.class
                );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to refresh Fitbit token");
        }

        updateTokens(response.getBody(), user);
    }

    // update stored tokens after refresh
    private void updateTokens(
            String responseBody,
            User user
    ) {
        try {
            Map<String, Object> tokenData =
                    objectMapper.readValue(
                            responseBody,
                            Map.class);

            user.setFitbitAccessToken(
                    (String) tokenData.get("access_token")
            );
            user.setFitbitRefreshToken(
                    (String) tokenData.get("refresh_token")
            );

            user.setFitbitTokenExpiry(
                    Instant.now().plusSeconds(
                            ((Number) tokenData.get("expires_in"))
                                    .longValue()
                    )
            );

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse refreshed Fitbit token",
                    e
            );
        }
    }


    /* ===================== AUTH ===================== */

    // get logged in user from security context
    private User getAuthenticatedUser() {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        UserDetailsImpl userDetails =
                (UserDetailsImpl) auth.getPrincipal();

        return userRepository
                .findById(userDetails.getId())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );
    }
}