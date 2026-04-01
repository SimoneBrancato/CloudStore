package service.impl;

import model.dto.UserDTO;
import model.dto.auth.AuthenticationResult;
import model.dto.auth.LoginResult;
import service.auth.JWTService;
import service.exception.ServiceException;
import service.interfaces.AuthService;
import service.interfaces.UserService;
import io.jsonwebtoken.Claims;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuthServiceImpl implements AuthService {

    private final UserService userService; // Dependency on UserService to retrieve user information
    private final JWTService jwtService; // Dependency on JWTService to handle token generation and validation


    /** 
        * Constructor for AuthServiceImpl.
        * @param userService The UserService instance to use for user operations.
        * @param jwtService The JWTService instance to use for token operations.
    **/
    public AuthServiceImpl(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /** 
        * Constructor for AuthServiceImpl.
        * @param userService The UserService instance to use for user operations.
        * @throws ServiceException If initialization fails.
    **/
    public AuthServiceImpl(UserService userService) throws ServiceException {
        this.userService = userService;
        this.jwtService = loadJWTServiceFromEnv();
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
        System.err.println("=== AUTHENTICATE USER ===");
        System.err.println("Nickname: " + nickname);
        System.err.println("Password: " + password);
        
        UserDTO user = authenticateCredentials(nickname, password);
        System.err.println("User found: " + user.getNickname());
        System.err.println("User permission: " + (user.getPermission() != null ? user.getPermission().getCategory() : "null"));

        String permissionCategory = user.getPermission() != null ? user.getPermission().getCategory() : "";
        boolean isAdmin = permissionCategory != null
                && permissionCategory.toLowerCase(Locale.ROOT).contains("admin");
        
        System.err.println("Is admin: " + isAdmin);

        List<String> roles = new ArrayList<>();
        roles.add("customer");
        if (isAdmin) {
            roles.add("admin");
        }

        String token = jwtService.generateToken(nickname, roles);
        System.err.println("Token generated successfully");

        return new LoginResult(token, user, roles, isAdmin);
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
            String nickname = claims.getSubject();
            List<String> roles = jwtService.extractRoles(claims);

            return new AuthenticationResult(nickname, roles);
        } catch (RuntimeException e) {
            throw new ServiceException("Identity verification failed: " + e.getMessage());
        }
    }

    /** 
        * Authenticates user credentials.
        * @param nickname The nickname of the user.
        * @param password The password of the user.
        * @return The authenticated UserDTO.
        * @throws ServiceException If authentication fails.
    **/
    private UserDTO authenticateCredentials(String nickname, String password) throws ServiceException {
        System.err.println("Checking credentials for: " + nickname);
        
        if (nickname == null || nickname.isBlank() || password == null || password.isBlank()) {
            System.err.println("Missing credentials");
            throw new ServiceException("Authentication failed: Missing credentials.");
        }

        UserDTO user = userService.findByNickname(nickname)
                .orElseThrow(() -> {
                    System.err.println("User not found: " + nickname);
                    return new ServiceException("Invalid credentials");
                });

        System.err.println("Stored password: " + user.getPassword());
        System.err.println("Provided password: " + password);
        
        if (!password.equals(user.getPassword())) {
            System.err.println("Password mismatch");
            throw new ServiceException("Invalid credentials");
        }
        
        System.err.println("Credentials valid");
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
            System.err.println("ERROR: JWT_SECRET environment variable is not set!");
            throw new IllegalArgumentException("CRITICAL: JWT_SECRET environment variable is not set.");
        }
        
        System.err.println("JWT_SECRET loaded (length: " + base64Secret.length() + ")");

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