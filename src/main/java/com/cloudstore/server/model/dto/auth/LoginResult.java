package com.cloudstore.server.model.dto.auth;

import com.cloudstore.server.model.dto.UserDTO;
import java.util.Collections;
import java.util.List;


public final class LoginResult {

    private final String token; // The authentication token for the logged-in user

    private final UserDTO user; // The user details of the logged-in user

    private final String role; // The highest priority role associated with the logged-in user

    private final boolean isAdmin; // Indicates whether the logged-in user has administrative privileges

    /**
        * Constructor for LoginResult.
        * @param token The authentication token for the logged-in user.
        * @param user The user details of the logged-in user.
        * @param role The highest priority role of the logged-in user.
        * @param isAdmin Indicates whether the logged-in user has administrative privileges.
    **/
    public LoginResult(String token, UserDTO user, String role, boolean isAdmin) {
        this.token = token;
        this.user = user;
        this.role = role;
        this.isAdmin = isAdmin;
    }

    /**
        * Gets the authentication token for the logged-in user.
        * @return The authentication token.
    **/
    public String getToken() {
        return token;
    }

    /**
        * Gets the user details of the logged-in user.
        * @return The user details.
    **/
    public UserDTO getUser() {
        return user;
    }

    /**
        * Gets the highest priority role associated with the logged-in user.
        * @return The single role.
    **/
    public String getRole() {
        return role;
    }
    
    /**
        * Indicates whether the logged-in user has administrative privileges.
        * @return True if the user is an admin, false otherwise.
    **/
    public boolean isAdmin() {
        return isAdmin;
    }
}