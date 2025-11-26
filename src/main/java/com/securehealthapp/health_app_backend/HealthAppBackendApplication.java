package com.securehealthapp.health_app_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@RestController
public class HealthAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthAppBackendApplication.class, args);
	}

	@GetMapping("/")
	public String home() {
		return "Welcome to the Secure Health App Backend!";
	}
}