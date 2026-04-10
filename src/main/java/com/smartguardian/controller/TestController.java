package com.smartguardian.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartguardian.payload.response.MessageResponse;


/* ===================== TEST CONTROLLER ===================== */

// browser remembers permission for 1 hour
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {

    // accessible without authentication
    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        return ResponseEntity.ok(new MessageResponse("This is a public endpoint!"));
    }

    // user endpoint
    @GetMapping("/user")
    public ResponseEntity<?> userEndpoint() {
        return ResponseEntity.ok(
                new MessageResponse("This is a protected user endpoint!")
        );
    }
}