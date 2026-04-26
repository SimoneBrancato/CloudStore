package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.dto.auth.LoginResult;
import com.cloudstore.server.model.dto.auth.AuthenticationResult;
import com.cloudstore.server.service.exception.ServiceException;

public interface AuthService {

    LoginResult authenticateUser(String nickname, String password) throws ServiceException;
    
    AuthenticationResult getSessionFromToken(String token) throws ServiceException;

    void logout(String token) throws ServiceException;
}