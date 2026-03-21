package com.cloudstore.service.interfaces;

import com.cloudstore.model.dto.auth.LoginResult;
import com.cloudstore.model.dto.auth.AuthenticationResult;
import com.cloudstore.service.exception.ServiceException;

/**
 * Service interface for authentication and authorization.
 */
public interface AuthService {

    /**
     * Logs in a user with nickname and password.
     * Returns a JWT token and basic user information.
     *
     * @param nickname Username
     * @param password Plain-text password
     * @return Object containing the token and user info
     * @throws ServiceException If credentials are wrong, account is locked, or a database error occurs
     */
    LoginResult authenticateUser(String nickname, String password) throws ServiceException;

    /**
     * Validates a JWT token (checks expiration, integrity, revocation).
     * Returns the user data extracted from the token.
     *
     * @param token JWT token to validate
     * @return Object with user identity, roles, and claims
     * @throws ServiceException If the token is invalid
     */
    AuthenticationResult authenticateByToken(String token) throws ServiceException;

    /**
     * Checks whether the given credentials belong to a user with admin rights.
     * Throws an exception if the check fails.
     *
     * @param nickname Username
     * @param password Plain-text password
     * @throws ServiceException If credentials are incorrect or the user is not an admin
     */
    void assertAdminAccess(String nickname, String password) throws ServiceException;
}