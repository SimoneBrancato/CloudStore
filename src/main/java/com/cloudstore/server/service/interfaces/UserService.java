package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.entities.User;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface UserService {
    
    Optional<User> findByNickname(String nickname) throws ServiceException;
    
    Optional<User> findByEmail(String email) throws ServiceException;
    
    List<User> findByPermission(int permissionId) throws ServiceException;
    
    List<User> findAll() throws ServiceException;
    
    User register(User user) throws ServiceException;
    
    boolean delete(String nickname) throws ServiceException;
    
    boolean updatePassword(String nickname, String newPassword) throws ServiceException;
    
    boolean updatePermission(String nickname, int permissionId) throws ServiceException;
    
    boolean exists(String nickname) throws ServiceException;
    
    int count() throws ServiceException;
    
    String resolveCustomerCategory(String customerName) throws ServiceException;
}