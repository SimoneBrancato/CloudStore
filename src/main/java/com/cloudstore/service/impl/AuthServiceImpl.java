package com.cloudstore.service.impl;

import com.cloudstore.model.dto.UserDTO;
import com.cloudstore.model.dto.auth.AuthenticationResult;
import com.cloudstore.model.dto.auth.LoginResult;
import com.cloudstore.service.auth.JWTService;
import com.cloudstore.service.exception.ServiceException;
import com.cloudstore.service.interfaces.AuthService;
import com.cloudstore.service.interfaces.UserService;
import io.jsonwebtoken.Claims;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of AuthService.
 * Handles login, token validation, and admin access checks.
 */
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JWTService jwtService;

    /**
     *
     * @param userService Service to access user data
     * @param jwtService  Service to generate and validate JWT tokens
     */
    public AuthServiceImpl(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Default constructor for manual instantiation.
     * Loads JWT configuration from environment variables.
     *
     * @throws ServiceException If JWT_SECRET is missing
     */
    public AuthServiceImpl() throws ServiceException {
        try {
            this.userService = new UserServiceImpl();
            this.jwtService = loadJWTServiceFromEnv();
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Initialization failed: JWT configuration is missing or invalid.", e);
        }
    }

    /**
     * Logs in a user with nickname and password.
     * Returns a JWT token, user data, roles, and admin flag.
     */
    @Override
    public LoginResult authenticateUser(String nickname, String password) throws ServiceException {
        UserDTO user = authenticateCredentials(nickname, password);

        // All users are "customer". If permission contains "admin", also add "admin" role.
        String permissionCategory = user.getPermission() != null ? user.getPermission().getCategory() : "";
        boolean isAdmin = permissionCategory != null
                && permissionCategory.toLowerCase(Locale.ROOT).contains("admin");

        List<String> roles = new ArrayList<>();
        roles.add("customer");
        if (isAdmin) {
            roles.add("admin");
        }

        String token = jwtService.generateToken(nickname, roles);

        return new LoginResult(token, user, roles, isAdmin);
    }

    /**
     * Validates a JWT token and checks that the user still exists in the database.
     * Returns the user's nickname and roles.
     */
    @Override
    public AuthenticationResult authenticateByToken(String token) throws ServiceException {
        try {
            Claims claims = jwtService.validateAndGetClaims(token);
            String nickname = claims.getSubject();
            List<String> roles = jwtService.extractRoles(claims);

            // Extra security: even if the token is valid, the user might have been deleted.
            if (!userService.exists(nickname)) {
                throw new ServiceException("Security alert: User identity no longer valid.");
            }

            return new AuthenticationResult(nickname, roles);
        } catch (RuntimeException e) {
            throw new ServiceException("Identity verification failed: " + e.getMessage());
        }
    }

    /**
     * Checks if the given credentials belong to an admin user.
     * Throws an exception if not.
     */
    @Override
    public void assertAdminAccess(String nickname, String password) throws ServiceException {
        UserDTO user = authenticateCredentials(nickname, password);
        
        String permissionCategory = user.getPermission() != null ? user.getPermission().getCategory() : "";
        boolean isAdmin = permissionCategory != null
                && permissionCategory.toLowerCase(Locale.ROOT).contains("admin");
                
        if (!isAdmin) {
            throw new ServiceException("Access denied: Higher privileges required for this operation.");
        }
    }

    /**
     * Helper method that checks nickname and password.
     * Returns the user if credentials are valid.
     *
     * @implNote Currently uses plain-text password comparison.
     * Convert in BCrypt or another encoder.
     */
    private UserDTO authenticateCredentials(String nickname, String password) throws ServiceException {
        if (nickname == null || nickname.isBlank() || password == null || password.isBlank()) {
            throw new ServiceException("Authentication failed: Missing credentials.");
        }

        UserDTO user = userService.findByNickname(nickname)
                .orElseThrow(() -> new ServiceException("Invalid credentials"));

        // Warning: Direct comparison is not secure. Use a password encoder in production.
        if (!password.equals(user.getPassword())) {
            throw new ServiceException("Invalid credentials");
        }
        return user;
    }

    /**
     * Reads JWT configuration from environment variables.
     */
    private JWTService loadJWTServiceFromEnv() {
        String base64Secret = System.getenv("JWT_SECRET");
        if (base64Secret == null || base64Secret.isEmpty()) {
            throw new IllegalArgumentException("CRITICAL: JWT_SECRET environment variable is not set.");
        }

        // Default expiration: 1 minute if not specified
        long expirationMs;
        try {
            expirationMs = Long.parseLong(System.getenv().getOrDefault("JWT_EXPIRATION_MS", "60000"));
        } catch (NumberFormatException e) {
            expirationMs = 60000;
        }

        String issuer = System.getenv().getOrDefault("JWT_ISSUER", "cloudstore-auth");
        String audience = System.getenv().getOrDefault("JWT_AUDIENCE", "cloudstore-clients");

        return new JWTService(base64Secret, expirationMs, issuer, audience);
    }
}