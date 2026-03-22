package com.cloudstore.model.dto.auth;

import com.cloudstore.model.dto.UserDTO;
import java.util.Collections;
import java.util.List;

/**
 * Contains the data returned after a successful login.
 * Includes the JWT token, user profile, roles, and admin flag.
 */
public final class LoginResult {

    /** JWT token to be used for authenticated requests. */
    private final String token;

    /** Profile information of the logged-in user. */
    private final UserDTO user;

    /** List of roles assigned to the user. */
    private final List<String> roles;

    /** Quick flag to check if the user is an admin. */
    private final boolean isAdmin;

    /**
     * Creates a new LoginResult.
     *
     * @param token   The authentication token
     * @param user    The user's profile data
     * @param roles   List of roles (if null, an empty list is stored)
     * @param isAdmin True if the user has admin privileges
     */
    public LoginResult(String token, UserDTO user, List<String> roles, boolean isAdmin) {
        this.token = token;
        this.user = user;
        this.roles = roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
        this.isAdmin = isAdmin;
    }

    public String getToken() {
        return token;
    }

    public UserDTO getUser() {
        return user;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}