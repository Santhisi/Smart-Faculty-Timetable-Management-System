package com.smartfaculty.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;
    private final String issuer;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs,
            @Value("${jwt.issuer:SmartFaculty}") String issuer
    ) {

        // -------------------------------------------------
        // VALIDATE SECRET
        // -------------------------------------------------

        if (secret == null ||
                secret.length() < 32) {

            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 characters long."
            );
        }


        // -------------------------------------------------
        // VALIDATE EXPIRATION
        // -------------------------------------------------

        if (expirationMs < 60_000) {

            throw new IllegalArgumentException(
                    "JWT expiration must be at least 1 minute."
            );
        }


        // -------------------------------------------------
        // VALIDATE ISSUER
        // -------------------------------------------------

        if (issuer == null ||
                issuer.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "JWT issuer is required."
            );
        }


        // -------------------------------------------------
        // CREATE SIGNING KEY
        // -------------------------------------------------

        this.key =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );


        // -------------------------------------------------
        // STORE CONFIGURATION
        // -------------------------------------------------

        this.expirationMs = expirationMs;

        this.issuer = issuer.trim();
    }


    // =====================================================
    // GENERATE JWT
    // =====================================================

    public String generateToken(String username) {

        // -------------------------------------------------
        // VALIDATE USERNAME
        // -------------------------------------------------

        if (username == null ||
                username.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required to generate a JWT."
            );
        }


        username = username.trim();


        // -------------------------------------------------
        // CREATE TOKEN
        // -------------------------------------------------

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + expirationMs
                        )
                )
                .signWith(key)
                .compact();
    }


    // =====================================================
    // EXTRACT USERNAME
    // =====================================================

    public String extractUsername(String token) {

        // -------------------------------------------------
        // VALIDATE TOKEN
        // -------------------------------------------------

        if (token == null ||
                token.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "JWT token is required."
            );
        }


        // -------------------------------------------------
        // EXTRACT USERNAME
        // -------------------------------------------------

        String username =
                extractAllClaims(token).getSubject();


        // -------------------------------------------------
        // VALIDATE USERNAME
        // -------------------------------------------------

        if (username == null ||
                username.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "JWT does not contain a valid username."
            );
        }


        return username.trim();
    }


    // =====================================================
    // VALIDATE JWT
    // =====================================================

    public boolean isTokenValid(
            String token,
            String username) {

        // -------------------------------------------------
        // VALIDATE TOKEN
        // -------------------------------------------------

        if (token == null ||
                token.trim().isEmpty()) {

            return false;
        }


        // -------------------------------------------------
        // VALIDATE USERNAME
        // -------------------------------------------------

        if (username == null ||
                username.trim().isEmpty()) {

            return false;
        }


        try {

            username = username.trim();


            // -------------------------------------------------
            // EXTRACT ALL CLAIMS
            // -------------------------------------------------

            Claims claims =
                    extractAllClaims(token);


            // -------------------------------------------------
            // CHECK SUBJECT
            // -------------------------------------------------

            String subject =
                    claims.getSubject();

            if (subject == null ||
                    subject.trim().isEmpty()) {

                return false;
            }


            // -------------------------------------------------
            // SUBJECT MUST MATCH USERNAME
            // -------------------------------------------------

            if (!subject.trim().equals(username)) {

                return false;
            }


            // -------------------------------------------------
            // CHECK USERNAME
            // -------------------------------------------------

            String extractedUsername =
                    extractUsername(token);

            if (extractedUsername == null ||
                    !extractedUsername.equals(username)) {

                return false;
            }


            // -------------------------------------------------
            // CHECK EXPIRATION
            // -------------------------------------------------

            if (isTokenExpired(token)) {

                return false;
            }


            // -------------------------------------------------
            // CHECK ISSUER
            // -------------------------------------------------

            String tokenIssuer =
                    claims.getIssuer();

            if (tokenIssuer == null ||
                    !tokenIssuer.equals(issuer)) {

                return false;
            }


            // -------------------------------------------------
            // CHECK ISSUED-AT TIME
            // -------------------------------------------------

            Date issuedAt =
                    claims.getIssuedAt();

            if (issuedAt == null) {

                return false;
            }


            // -------------------------------------------------
            // ISSUED-AT CANNOT BE IN THE FUTURE
            // -------------------------------------------------

            if (issuedAt.after(new Date())) {

                return false;
            }


            // -------------------------------------------------
            // GET TOKEN EXPIRATION
            // -------------------------------------------------

            Date expiration =
                    claims.getExpiration();

            if (expiration == null) {

                return false;
            }


            // -------------------------------------------------
            // ISSUED-AT MUST BE BEFORE EXPIRATION
            // -------------------------------------------------

            if (!issuedAt.before(expiration)) {

                return false;
            }


            return true;

        } catch (Exception e) {

            return false;
        }
    }


    // =====================================================
    // EXTRACT ALL CLAIMS
    // =====================================================

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    // =====================================================
    // CHECK TOKEN EXPIRATION
    // =====================================================

    private boolean isTokenExpired(String token) {

        Date expiration =
                extractAllClaims(token)
                        .getExpiration();

        if (expiration == null) {

            return true;
        }


        return expiration.before(
                new Date()
        );
    }
}