package com.cloudstore.service.auth.rbac;

import com.cloudstore.model.dto.UserDTO;
import com.cloudstore.model.dto.auth.AuthenticationResult;
import com.cloudstore.model.entities.Role;
import com.cloudstore.service.exception.ServiceException;
import com.cloudstore.service.interfaces.AuthService;
import com.cloudstore.service.interfaces.UserService;

import java.util.Optional;

public class AccessControl {

    private final AuthService authService;
    private final UserService userService;

    public AccessControl(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Valida il token e verifica che il chiamante abbia almeno il ruolo richiesto.
     * Admin può fare tutto ciò che possono fare Seller e Customer
     *
     * @param token    JWT/token dell'utente
     * @param required ruolo minimo necessario per l'operazione
     * @return UserDTO dell'utente autenticato
     * @throws ServiceException se il token non è valido o il ruolo è insufficiente
     */
    public UserDTO requireRole(String token, Role required) throws ServiceException {
        UserDTO user = resolveUser(token);
        Role callerRole = resolveRole(user);

        if (!callerRole.hasAccessTo(required)) {
            throw new ServiceException(String.format(
                "Access denied: operation requires '%s' role, but '%s' has '%s'",
                required, user.getNickname(), callerRole
            ));
        }
        return user;
    }

    private UserDTO resolveUser(String token) throws ServiceException {
        if (token == null || token.isBlank()) {
            throw new ServiceException("Authentication token required");
        }

        AuthenticationResult auth;

        try {
            auth = authService.authenticateByToken(token);
        } catch (Exception e) {
            throw new ServiceException("Invalid or expired token", e);
        }

        if (auth == null || auth.getNickname() == null || auth.getNickname().isBlank()) {
            throw new ServiceException("Invalid or expired token");
        }

        Optional<UserDTO> userOpt = userService.findByNickname(auth.getNickname());
        return userOpt.orElseThrow(() -> new ServiceException("User not found"));
    }

    public Role resolveRole(UserDTO user) {
        if (user.getPermission() == null || user.getPermission().getCategory() == null) {
            return Role.CUSTOMER;
        }
        return Role.fromCategory(user.getPermission().getCategory());
    }
}