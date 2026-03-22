package com.cloudstore.model.dto.auth;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Holds the result of a successful token validation.
 * Contains the username and roles of the authenticated user.
 * This object is immutable (can't be changed after creation).
 */
public final class AuthenticationResult {

    /** The username of the authenticated user. */
    private final String nickname;

    /** List of roles assigned to the user. */
    private final List<String> roles;

    /**
     * Creates a new AuthenticationResult.
     *
     * @param nickname The username
     * @param roles    List of roles (if null, an empty list is stored)
     */
    public AuthenticationResult(String nickname, List<String> roles) {
        this.nickname = nickname;
        // Store an unmodifiable copy of the list so it can't be modified from outside
        this.roles = roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
    }

    public String getNickname() {
        return nickname;
    }

    /**
     * Returns the roles as an unmodifiable list.
     */
    public List<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthenticationResult)) return false;
        AuthenticationResult that = (AuthenticationResult) o;
        return Objects.equals(nickname, that.nickname) &&
               Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname, roles);
    }

    @Override
    public String toString() {
        return "AuthenticationResult{" +
               "nickname='" + nickname + '\'' +
               ", roles=" + roles +
               '}';
    }
}