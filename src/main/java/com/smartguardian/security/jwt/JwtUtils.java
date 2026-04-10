package com.smartguardian.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.smartguardian.security.services.UserDetailsImpl;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;


/* ===================== JWT UTILS ===================== */

@Component
public class JwtUtils {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtUtils.class);

    // Config
    @Value("${smartguardian.app.jwtSecret}")
    private String jwtSecret;

    @Value("${smartguardian.app.jwtExpirationMs}")
    private int jwtExpirationMs;


    /* ===================== GENERATE TOKEN ===================== */

    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal =
                (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                (new Date()).getTime()
                                        + jwtExpirationMs
                        )
                )
                .signWith(key())
                .compact();
    }



    /* ===================== SECRET KEY ===================== */

    // converts secret string into a cryptographic key
    private SecretKey key() {

        return Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }


    /* ===================== GET USERNAME ===================== */

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    /* ===================== VALIDATE TOKEN ===================== */

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);

            return true;

        } catch (Exception e) {
            logger.error(
                    "Invalid JWT token: {}",
                    e.getMessage());
            return false;
        }
    }
}