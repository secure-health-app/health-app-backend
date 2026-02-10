package com.smartguardian.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartguardian.payload.response.MessageResponse;

@CrossOrigin(origins = "*", maxAge = 3600) // browser remembers permission for 1 hour
@RestController
@RequestMapping("/api/test")
public class TestController {

  @GetMapping("/public")
  public ResponseEntity<?> publicEndpoint() {
    return ResponseEntity.ok(new MessageResponse("This is a public endpoint!"));
  }

  @GetMapping("/user")
  public ResponseEntity<?> userEndpoint() {
    return ResponseEntity.ok(new MessageResponse("This is a protected user endpoint!"));
  }
}