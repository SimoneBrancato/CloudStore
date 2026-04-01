package model.dto.auth;

import java.util.Collections;
import java.util.List;
import java.util.Objects;


public final class AuthenticationResult {

    private final String nickname; // The nickname of the authenticated user
    private final List<String> roles; // The roles associated with the authenticated user

    /**
        * Constructor for AuthenticationResult.
        * @param nickname The nickname of the authenticated user.
        * @param roles The roles associated with the authenticated user. If null, it will be treated as an empty list.
    **/
    public AuthenticationResult(String nickname, List<String> roles) {
        this.nickname = nickname;
        this.roles = roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
    }

    /**
        * Gets the nickname of the authenticated user.
        * @return The nickname.
    **/
    public String getNickname() {
        return nickname;
    }

    /**
        * Gets the roles associated with the authenticated user.
        * @return The list of roles.
    **/
    public List<String> getRoles() {
        return roles;
    }

    /**
        * Indicates whether the authenticated user has administrative privileges.
        * @return True if the user is an admin, false otherwise.
    **/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthenticationResult)) return false;
        AuthenticationResult that = (AuthenticationResult) o;
        return Objects.equals(nickname, that.nickname) &&
               Objects.equals(roles, that.roles);
    }

    /**
        * Generates a hash code for the authenticated user.
        * @return The hash code.
    **/
    @Override
    public int hashCode() {
        return Objects.hash(nickname, roles);
    }

    /**
        * Generates a string representation of the authenticated user.
        * @return The string representation.
    **/
    @Override
    public String toString() {
        return "AuthenticationResult{" +
               "nickname='" + nickname + '\'' +
               ", roles=" + roles +
               '}';
    }
}