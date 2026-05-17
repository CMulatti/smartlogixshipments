package com.smartlogix.shipmentservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    //We use this secret to sign the token

    private static final String SECRET = "myverysecretkeythatisatleast256bitslong12345";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXPIRATION_TIME = 86400000; // token lasts 24 hours (in milliseconds)


    //Validate and extract username from token: Verify token with Secret key, decode it, read the subject value
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //Extract role from token
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    // Check if token is valid
    //if token is expired, tampered, wrong key, or corrupted, an exception occurs and returns false
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            System.err.println("Validación de token fallida " + e.getMessage());
            return false;
        }
    }
}