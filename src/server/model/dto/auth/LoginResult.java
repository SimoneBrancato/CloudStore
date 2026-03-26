package model.dto.auth;

import model.dto.UserDTO;
import java.util.Collections;
import java.util.List;


public final class LoginResult {

    private final String token; // The authentication token for the logged-in user

    private final UserDTO user; // The user details of the logged-in user

    private final List<String> roles; // The roles associated with the logged-in user

    private final boolean isAdmin; // Indicates whether the logged-in user has administrative privileges

    /**
        * Constructor for LoginResult.
        * @param token The authentication token for the logged-in user.
        * @param user The user details of the logged-in user.
        * @param roles The roles associated with the logged-in user. If null, it will be treated as an empty list.
        * @param isAdmin Indicates whether the logged-in user has administrative privileges.
    **/
    public LoginResult(String token, UserDTO user, List<String> roles, boolean isAdmin) {
        this.token = token;
        this.user = user;
        this.roles = roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
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
        * Gets the roles associated with the logged-in user.
        * @return The list of roles.
    **/
    public List<String> getRoles() {
        return roles;
    }
    
    /**
        * Indicates whether the logged-in user has administrative privileges.
        * @return True if the user is an admin, false otherwise.
    **/
    public boolean isAdmin() {
        return isAdmin;
    }
}