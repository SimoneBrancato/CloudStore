package com.cloudstore.server.service.security;

import com.cloudstore.server.model.dto.auth.LoginResult;
import com.cloudstore.server.model.dto.auth.AuthenticationResult;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.exception.UnauthorizedException;

public class SecurityContext {
    
    // ThreadLocal to store the current authenticated user for each thread.
    private static final ThreadLocal<AuthenticationResult> currentUser = new ThreadLocal<>();

    /**
        * Sets the current authenticated user for the thread.
        * @param login The AuthenticationResult containing user details and permissions.
    **/
    public static void set(AuthenticationResult login) {
        currentUser.set(login);
    }

    /**
        * Retrieves the current authenticated user for the thread.
        * @return The AuthenticationResult of the current user, or null if not authenticated.
    **/
    public static AuthenticationResult get() {
        return currentUser.get();
    }

    /**
        * Clears the current authenticated user from the thread context.
        * This should be called after the request is processed to prevent memory leaks.
    **/
    public static void clear() {
        currentUser.remove();
    }

    /**
        * Asserts that the current thread has an authenticated user.
        * @throws ServiceException if there is no authenticated user in the context.
    **/
    public static void assertAuthenticated() throws ServiceException {
        if (get() == null) {
            throw new UnauthorizedException("Unauthorized: Missing or invalid authentication token.");
        }
    }
}