package com.cloudstore.server.service.impl;

import com.cloudstore.server.model.dto.UserDTO;
import com.cloudstore.server.model.dto.auth.AuthenticationResult;
import com.cloudstore.server.model.dto.auth.LoginResult;
import com.cloudstore.server.model.entities.User;
import com.cloudstore.server.service.auth.JWTService;
import com.cloudstore.server.service.mapper.DTOMapper;
import com.cloudstore.server.service.auth.PasswordHasher;
import com.cloudstore.server.service.auth.TokenBlacklistService;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.exception.UnauthorizedException;
import com.cloudstore.server.service.exception.ValidationException;
import com.cloudstore.server.service.interfaces.AuthService;
import com.cloudstore.server.service.interfaces.UserService;
import com.cloudstore.server.model.auth.Role;
import io.jsonwebtoken.Claims;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuthServiceImpl implements AuthService {

    private final UserService userService; // Dependency on UserService to retrieve user information
    private final JWTService jwtService; // Dependency on JWTService to handle token generation and validation
    private final TokenBlacklistService blacklistService; // Dependency on TokenBlacklistService to handle token revocation


    /** 
        * Constructor for AuthServiceImpl.
        * @param userService The UserService instance to use for user operations.
        * @param jwtService The JWTService instance to use for token operations.
    **/
    public AuthServiceImpl(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.blacklistService = new TokenBlacklistService();
    }

    /** 
        * Constructor for AuthServiceImpl.
        * @param userService The UserService instance to use for user operations.
        * @throws ServiceException If initialization fails.
    **/
    public AuthServiceImpl(UserService userService) throws ServiceException {
        this.userService = userService;
        this.jwtService = loadJWTServiceFromEnv();
        this.blacklistService = new TokenBlacklistService();
    }

    /** 
        * Default constructor for AuthServiceImpl.
        * Initializes UserService and JWTService with default implementations and environment configuration.
        * @throws ServiceException If initialization fails due to missing JWT configuration or other issues.
    **/
    public AuthServiceImpl() throws ServiceException {
        try {
            this.userService = new UserServiceImpl();
            this.jwtService = loadJWTServiceFromEnv();
            this.blacklistService = new TokenBlacklistService();
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Initialization failed: JWT configuration is missing or invalid.", e);
        }
    }

    /** 
        * Authenticates a user based on provided nickname and password.
        * @param nickname The nickname of the user attempting to authenticate.
        * @param password The password of the user attempting to authenticate.
        * @return A LoginResult containing the authentication token, user details, roles, and admin status.
        * @throws ServiceException If authentication fails due to invalid credentials or other issues.
    **/
    @Override
    public LoginResult authenticateUser(String nickname, String password) throws ServiceException {
        
        User user = authenticateCredentials(nickname, password);

        Role assignedRole = Role.CUSTOMER;
        if (user.PermissionID() != null) {
            assignedRole = Role.fromId(user.PermissionID().id());
        }

        boolean isAdmin = (assignedRole == Role.ADMIN);

        List<String> roles = new ArrayList<>();
        roles.add(assignedRole.getRoleName());

        String token = jwtService.generateToken(nickname, roles);

        String role = roles.isEmpty() ? "customer" : roles.get(0);
        return new LoginResult(token, DTOMapper.toDTO(user), role, isAdmin);
    }

    /** 
        * Retrieves the authentication session from a given token.
        * @param token The JWT token to validate and extract session information from.
        * @return An AuthenticationResult containing the user's nickname and roles if the token is valid.
        * @throws ServiceException If the token is invalid or if identity verification fails.
    **/
    @Override
    public AuthenticationResult getSessionFromToken(String token) throws ServiceException {
        try {
            Claims claims = jwtService.validateAndGetClaims(token);

            // Check if the token has been revoked (blacklisted via logout)
            String jti = claims.getId();
            if (jti != null && blacklistService.isRevoked(jti)) {
                throw new UnauthorizedException("Token has been revoked");
            }

            String nickname = claims.getSubject();
            List<String> roles = jwtService.extractRoles(claims);

            return new AuthenticationResult(nickname, roles);
        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Identity verification failed: " + e.getMessage());
        }
    }

    /** 
        * Logs out the user by blacklisting the token's JTI in Redis.
        * The token will be rejected on subsequent requests until it naturally expires.
        * @param token The JWT token to invalidate.
        * @throws ServiceException If the token is invalid or cannot be parsed.
    **/
    @Override
    public void logout(String token) throws ServiceException {
        try {
            Claims claims = jwtService.validateAndGetClaims(token);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();

            if (jti == null || expiration == null) {
                return; // Token without JTI or expiration — nothing to blacklist
            }

            long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            blacklistService.revoke(jti, ttlSeconds);
        } catch (RuntimeException e) {
            throw new ServiceException("Logout failed: " + e.getMessage(), e);
        }
    }

    /** 
        * Authenticates user credentials.
        * @param nickname The nickname of the user.
        * @param password The password of the user.
        * @return The authenticated UserDTO.
        * @throws ServiceException If authentication fails.
    **/
    private User authenticateCredentials(String nickname, String password) throws ServiceException {
        
        if (nickname == null || nickname.isBlank() || password == null || password.isBlank()) {
            throw new ValidationException("Authentication failed: Missing credentials.");
        }

        User user = userService.findByNickname(nickname)
                .orElseThrow(() -> {
                    return new UnauthorizedException("Invalid credentials");
                });
        
        String storedPassword = user.password();
        boolean validPassword = PasswordHasher.matches(password, storedPassword) || password.equals(storedPassword);
        if (!validPassword) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        return user;
    }

    /** 
        * Loads the JWT service configuration from environment variables.
        * @return The configured JWTService instance.
        * @throws IllegalArgumentException If the JWT_SECRET environment variable is not set.
    **/
    private JWTService loadJWTServiceFromEnv() {
        String base64Secret = System.getenv("JWT_SECRET");
        if (base64Secret == null || base64Secret.isEmpty()) {
            throw new IllegalArgumentException("CRITICAL: JWT_SECRET environment variable is not set.");
        }

        long expirationMs;
        try {
            expirationMs = Long.parseLong(System.getenv().getOrDefault("JWT_EXPIRATION_MS", "3600000"));
        } catch (NumberFormatException e) {
            expirationMs = 3600000;
        }

        String issuer = System.getenv().getOrDefault("JWT_ISSUER", "cloudstore-auth");
        String audience = System.getenv().getOrDefault("JWT_AUDIENCE", "cloudstore-client");

        return new JWTService(base64Secret, expirationMs, issuer, audience);
    }
}