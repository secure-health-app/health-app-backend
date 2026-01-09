package com.smartguardian.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth/fitbit")
public class FitbitAuthController {

    @Value("${fitbit.client-id}")
    private String clientId;

    @Value("${fitbit.client-secret}")
    private String clientSecret;

    @Value("${fitbit.redirect-uri}")
    private String redirectUri;

    @GetMapping("/connect")
    public void connectToFitbit(HttpServletResponse response) throws IOException {

        String scopes = "activity heartrate sleep profile";

        String authUrl =
                "https://www.fitbit.com/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(scopes, StandardCharsets.UTF_8);

        response.sendRedirect(authUrl);
    }

    @GetMapping("/callback")
    public ResponseEntity<?> fitbitCallback(@RequestParam("code") String code) {
        String tokenUrl = "https://api.fitbit.com/oauth2/token";
        
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("clientId", clientId);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", redirectUri);
        map.add("code", code);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error exchanging token: " + e.getMessage());
        }
    }
}
