package com.smartguardian.security.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartguardian.model.User;
import com.smartguardian.repository.UserRepository;
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

    private final UserRepository userRepository;

    public FitbitAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.client-secret}")
    private String clientSecret;

    @Value("${fitbit.redirect-uri}")
    private String redirectUri;

    public void exchangeCodeAndSaveTokens(String code, User user) throws Exception {

        String tokenUrl = "https://api.fitbit.com/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String auth = clientId + ":" + clientSecret;
        headers.set(
                "Authorization",
                "Basic " + Base64.getEncoder()
                        .encodeToString(auth.getBytes(StandardCharsets.UTF_8))
        );

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", redirectUri.trim());
        body.add("code", code);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(tokenUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> tokenData =
                mapper.readValue(response.getBody(), Map.class);

        // Extract values SAFELY
        String accessToken = (String) tokenData.get("access_token");
        String refreshToken = (String) tokenData.get("refresh_token");
        Integer expiresIn = (Integer) tokenData.get("expires_in");
        String fitbitUserId = (String) tokenData.get("user_id");

        // Persist to user
        user.setFitbitAccessToken(accessToken);
        user.setFitbitRefreshToken(refreshToken);
        user.setFitbitTokenExpiry(
                Instant.now().plusSeconds(expiresIn)
        );
        user.setFitbitUserId(fitbitUserId);

        userRepository.save(user);
    }
}
