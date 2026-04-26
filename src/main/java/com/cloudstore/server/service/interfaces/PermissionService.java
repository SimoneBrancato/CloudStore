package com.cloudstore.server.service.interfaces;

import com.cloudstore.server.model.entities.Permission;
import com.cloudstore.server.service.exception.ServiceException;

import java.util.List;
import java.util.Optional;

public interface PermissionService {
    
    Optional<Permission> findById(int id) throws ServiceException;

    Optional<Permission> findByCategory(String category) throws ServiceException;

    List<Permission> findAll() throws ServiceException;

    Permission save(Permission permission) throws ServiceException;

    boolean delete(int id) throws ServiceException;

    boolean exists(int id) throws ServiceException;

    int count() throws ServiceException;
}