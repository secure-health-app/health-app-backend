package com.smartguardian.security.services;

import com.smartguardian.model.User;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
public class FitbitApiService {

    private static final String FITBIT_BASE_URL =
            "https://api.fitbit.com/1/user/-/activities/heart/date/";

    private boolean isTokenExpired(User user) {
        return user.getFitbitTokenExpiry() == null ||
                Instant.now().isAfter(user.getFitbitTokenExpiry());
    }

    public String getHeartRateForDate(User user, String date) {

        String url = FITBIT_BASE_URL + date + "/1d.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getFitbitAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        String.class
                );

        return response.getBody();
    }
}
