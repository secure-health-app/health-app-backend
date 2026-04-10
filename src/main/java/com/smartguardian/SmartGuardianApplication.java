package com.smartguardian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/* ===================== APPLICATION ===================== */

@SpringBootApplication
@RestController
public class SmartGuardianApplication {


    /* ===================== MAIN ===================== */

    public static void main(String[] args) {

        SpringApplication.run(SmartGuardianApplication.class, args);
    }

    @GetMapping("/")
    public String home() {

        return "Welcome to SmartGuardian - Your AI Health Companion!";
    }
}