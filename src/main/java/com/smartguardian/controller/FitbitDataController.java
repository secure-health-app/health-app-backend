package com.smartguardian.controller;

import com.smartguardian.model.User;
import com.smartguardian.repository.UserRepository;
import com.smartguardian.security.services.UserDetailsImpl;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fitbit")
public class FitbitDataController {

    private final UserRepository userRepository;

    public FitbitDataController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/heart-rate")
    public ResponseEntity<?> getHeartRate(Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow();

        String accessToken = user.getFitbitAccessToken();

        String date = LocalDate.now().toString();

        String url =
                "https://api.fitbit.com/1/user/-/activities/heart/date/" +
                        date + "/1d.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return ResponseEntity.ok(response.getBody());
    }
}
