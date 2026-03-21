package com.cloudstore.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import com.cloudstore.service.auth.exception.JwtExpiredException;
import com.cloudstore.service.auth.exception.JwtInvalidException;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Centralized service for JWT (JSON Web Token) operations.
 * Handles token generation, validation, and claim extraction.
 * Designed to be thread-safe and immutable after construction.
 * Supports Role-Based Access Control (RBAC) with multiple roles.
 */
public class JWTService {

    // Immutable configuration fields
    private final long expirationTimeMs;   // Token validity duration in milliseconds
    private final String issuer;           // Required issuer claim (e.g., "cloudstore-api")
    private final String audience;         // Required audience claim (e.g., "cloudstore-client")
    private final SecretKey signingKey;    // HMAC key derived from base64 secret
    private final JwtParser jwtParser;     // Pre-configured parser with strict validation

    /**
     * Constructs the JWT service with mandatory configuration.
     * Fail-fast validation ensures all parameters are properly set.
     *
     * @param base64Secret     Base64-encoded secret (must decode to at least 32 bytes for HS256)
     * @param expirationTimeMs Token expiration in milliseconds (must be > 0)
     * @param issuer           Issuer identifier (must not be null/blank)
     * @param audience         Intended audience (must not be null/blank)
     * @throws IllegalArgumentException if any configuration is invalid
     */
    public JWTService(String base64Secret, long expirationTimeMs, String issuer, String audience) {
        // Fail-fast: secret must be present
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalArgumentException("CRITICAL: The JWT secret cannot be null or empty.");
        }
        // Expiration must be positive
        if (expirationTimeMs <= 0) {
            throw new IllegalArgumentException("CRITICAL: The expiration time must be greater than zero.");
        }
        // Issuer and audience are mandatory to prevent misconfiguration
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("CRITICAL: The issuer cannot be null or empty. Check your configuration.");
        }
        if (audience == null || audience.isBlank()) {
            throw new IllegalArgumentException("CRITICAL: The audience cannot be null or empty. Check your configuration.");
        }

        this.expirationTimeMs = expirationTimeMs;
        this.issuer = issuer;
        this.audience = audience;

        // Decode and validate key length (HS256 requires at least 256 bits = 32 bytes)
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("CRITICAL: The decoded key must be at least 32 bytes (256 bits) long.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        // Build parser with all required validations and a clock skew tolerance of 30 seconds
        this.jwtParser = Jwts.parser()
                .verifyWith(this.signingKey)
                .requireIssuer(this.issuer)          // Enforce issuer match
                .requireAudience(this.audience)      // Enforce audience match
                .clockSkewSeconds(30)                // Allow small time differences
                .build();
    }
    /**
     * Generates a signed JWT for an authenticated user with RBAC roles.
     *
     * @param nickname Unique user identifier (will become the subject claim)
     * @param roles    Collection of user roles (stored as a custom claim "roles")
     * @return Compact JWT string
     * @throws IllegalArgumentException if nickname is null/blank or roles collection is null/empty
     */
    public String generateToken(String nickname, Collection<String> roles) {
        // Mandatory fields – no insecure defaults
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("The nickname cannot be null or empty for token generation.");
        }
        // RBAC validation: a user must have at least one role
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required for token generation.");
        }

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + expirationTimeMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())     // Unique JWT ID (JTI) – helps prevent replay
                .subject(nickname)                    // Sub = user identifier
                .audience().add(audience).and()       // Set the configured audience
                .claim("roles", roles)                // Custom claim for RBAC roles (plural)
                .issuedAt(now)                        // Issued at (IAT)
                .expiration(expiryDate)               // Expiration (EXP)
                .issuer(issuer)                       // Issuer (ISS)
                .signWith(signingKey, Jwts.SIG.HS256) // Sign with HMAC-SHA256
                .compact();
    }

    /**
     * Validates a JWT and returns its claims.
     * Validation includes signature, expiration, issuer, and audience.
     *
     * @param token JWT string to validate
     * @return Claims payload of the token
     * @throws JwtExpiredException if the token has expired
     * @throws JwtInvalidException if the token is invalid (signature, structure, or claims mismatch)
     */
    public Claims validateAndGetClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Empty or missing JWT token");
        }

        try {
            // parseSignedClaims automatically validates signature, expiration, issuer, audience
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            // Differentiate expiration from other errors
            throw new JwtExpiredException("The JWT token has expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            // Catch all JWT-related errors and wrap them in our domain exception
            throw new JwtInvalidException("The JWT token is invalid or has been compromised", e);
        }
    }

    /**
     * Extracts the user nickname (subject) from claims.
     *
     * @param claims Valid claims object (e.g., from validateAndGetClaims)
     * @return The nickname stored in the subject
     */
    public String extractNickname(Claims claims) {
        return claims.getSubject();
    }
    
    /**
     * Extracts the collection of user roles from claims for RBAC checks.
     *
     * @param claims Valid claims object
     * @return A list of roles stored in the "roles" claim
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        return claims.get("roles", List.class);
    }
}