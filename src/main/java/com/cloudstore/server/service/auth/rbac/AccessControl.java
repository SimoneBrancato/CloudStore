package com.cloudstore.server.service.auth.rbac;

import com.cloudstore.server.model.dto.auth.AuthenticationResult;
import com.cloudstore.server.model.auth.Role;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.exception.ForbiddenException;
import com.cloudstore.server.service.security.SecurityContext;

import java.util.List;

public class AccessControl {

    // Default constructor
    public AccessControl() {
    }

    /** 
        * Verify that the currently authenticated user has the required role to perform an operation.
        * If the user is not authenticated or does not have sufficient privileges, a ServiceException is thrown.
        * @param required The role required to perform the operation.
        * @return The AuthenticationResult of the currently authenticated user if access is granted.
        * @throws ServiceException if the user is not authenticated or does not have the required role.
    **/
    public AuthenticationResult requireRole(Role required) throws ServiceException {
        SecurityContext.assertAuthenticated();
        AuthenticationResult auth = SecurityContext.get();
        Role callerRole = resolveRole(auth.getRoles());

        if (!callerRole.hasAccessTo(required)) {
            throw new ForbiddenException(String.format(
                "Access denied: operation requires '%s' role, but '%s' has '%s'",
                required, auth.getNickname(), callerRole
            ));
        }
        return auth;
    }

    /** 
        * Resolves the highest role from a list of role strings.
        * If the list is null or empty, it defaults to CUSTOMER.
        * @param roles The list of role strings to resolve.
        * @return The highest Role enum value corresponding to the provided role strings.
    **/
    public Role resolveRole(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Role.CUSTOMER;
        }
        
        Role highestRole = Role.CUSTOMER;
        for (String roleStr : roles) {
            Role role = Role.fromCategory(roleStr);
            if (role.hasAccessTo(highestRole)) {
                highestRole = role;
            }
        }
        return highestRole;
    }
}