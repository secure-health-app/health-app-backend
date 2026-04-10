package com.smartguardian.security;

import com.smartguardian.security.jwt.AuthEntryPointJwt;
import com.smartguardian.security.jwt.AuthTokenFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/* ===================== SECURITY CONFIG ===================== */

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;

    public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler) {

        this.unauthorizedHandler = unauthorizedHandler;
    }


    /* ===================== FILTER ===================== */

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {

        return new AuthTokenFilter();
    }


    /* ===================== AUTH MANAGER ===================== */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    /* ===================== PASSWORD ENCODER ===================== */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    /* ===================== SECURITY FILTER CHAIN ===================== */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(unauthorizedHandler)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth ->

                        auth.requestMatchers(
                                        org.springframework.http.HttpMethod.OPTIONS, "/**"
                                ).permitAll()

                                .requestMatchers(
                                        org.springframework.http.HttpMethod.POST,
                                        "/api/auth/signin",
                                        "/api/auth/signup"
                                ).permitAll()

                                .requestMatchers("/api/auth/fitbit/**").permitAll()

                                .requestMatchers("/api/test/public").permitAll()
                                .requestMatchers("/favicon.ico").permitAll()
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/error").permitAll()

                                // pi posts fall alerts — secured by device API key, not JWT
                                .requestMatchers(
                                        org.springframework.http.HttpMethod.POST,
                                        "/api/alerts/fall"
                                ).permitAll()

                                // all other alert endpoints require login (patient or caregiver)
                                .anyRequest().authenticated()
                );

        http.addFilterBefore(
                authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class
        );
        return http.build();
    }


    /* ===================== CORS ===================== */

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {

        org.springframework.web.cors.CorsConfiguration configuration =
                new org.springframework.web.cors.CorsConfiguration();

        configuration.setAllowedOriginPatterns(
                java.util.List.of("*"));

        configuration.setAllowedMethods(
                java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(
                java.util.List.of("*"));

        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );
        return source;
    }
}