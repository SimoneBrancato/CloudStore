package com.cloudstore.server.service.auth.rbac;

import com.cloudstore.server.model.dto.auth.AuthenticationResult;
import com.cloudstore.server.model.entities.Role;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.security.SecurityContext;

import java.util.List;

public class AccessControl {

    public AccessControl() {
    }

    /**
     * Valida il contesto di sicurezza e verifica che il chiamante abbia almeno il ruolo richiesto.
     * Admin può fare tutto ciò che possono fare Seller e Customer
     *
     * @param required ruolo minimo necessario per l'operazione
     * @return AuthenticationResult dell'utente autenticato
     * @throws ServiceException se il token non è presente o il ruolo è insufficiente
     */
    public AuthenticationResult requireRole(Role required) throws ServiceException {
        SecurityContext.assertAuthenticated();
        AuthenticationResult auth = SecurityContext.get();
        Role callerRole = resolveRole(auth.getRoles());

        if (!callerRole.hasAccessTo(required)) {
            throw new ServiceException(String.format(
                "Access denied: operation requires '%s' role, but '%s' has '%s'",
                required, auth.getNickname(), callerRole
            ));
        }
        return auth;
    }

    /**
     * Estrae il ruolo di maggior privilegio tra quelli assegnati all'utente.
     */
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