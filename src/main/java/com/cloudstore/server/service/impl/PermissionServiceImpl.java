package com.cloudstore.server.service.impl;

import com.cloudstore.server.dao.impl.PermissionDAOImpl;
import com.cloudstore.server.dao.interfaces.PermissionDAO;
import com.cloudstore.server.model.dto.PermissionDTO;
import com.cloudstore.server.model.entities.Permission;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.PermissionService;
import com.cloudstore.server.service.mapper.DTOMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionServiceImpl implements PermissionService {

    // Dependency on PermissionDAO to perform database operations related to permissions
    private final PermissionDAO permissionDAO;

    /** 
     * Constructor for PermissionServiceImpl that accepts a PermissionDAO instance.
     * @param permissionDAO The PermissionDAO instance to use for database operations.
    **/
    public PermissionServiceImpl(PermissionDAO permissionDAO) {
        this.permissionDAO = permissionDAO;
    }

    /** 
        * Default constructor for PermissionServiceImpl that initializes the PermissionDAO with a default implementation.
        * @throws ServiceException If initialization fails due to database connection issues or other problems.
    **/
    public PermissionServiceImpl() throws ServiceException {
        try {
            this.permissionDAO = new PermissionDAOImpl();
        } catch (SQLException e) {
            throw new ServiceException("Unable to initialize PermissionService", e);
        }
    }

    /** 
        * Retrieves a permission by its unique identifier.
        * @param id The unique identifier of the permission to retrieve.
        * @return An Optional containing the PermissionDTO if found, or empty if not found.
        * @throws ServiceException If an error occurs while retrieving the permission from the database.
    **/
    @Override
    public Optional<PermissionDTO> findById(int id) throws ServiceException {
        try {
            return permissionDAO.findById(id).map(DTOMapper::toDTO);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving permission with ID: " + id, e);
        }
    }

    /** 
        * Retrieves a permission by its category name.
        * @param category The category name of the permission to retrieve.
        * @return An Optional containing the PermissionDTO if found, or empty if not found.
        * @throws ServiceException If an error occurs while retrieving the permission from the database.
    **/
    @Override
    public Optional<PermissionDTO> findByCategory(String category) throws ServiceException {
        try {
            return permissionDAO.findByCategory(category).map(DTOMapper::toDTO);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving permission by category: " + category, e);
        }
    }

    /** 
        * Retrieves all permissions from the database.
        * @return A list of PermissionDTOs representing all permissions.
        * @throws ServiceException If an error occurs while retrieving permissions from the database.
    **/
    @Override
    public List<PermissionDTO> findAll() throws ServiceException {
        try {
            return permissionDAO.findAll().stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving permissions", e);
        }
    }

    /** 
        * Saves a permission to the database. If the permission already exists, it will be updated; otherwise, a new permission will be created.
        * @param dto The PermissionDTO containing the permission data to save.
        * @return The saved PermissionDTO with any generated fields (e.g., ID) populated.
        * @throws ServiceException If an error occurs while saving the permission to the database, such as validation errors or SQL exceptions.
    **/
    @Override
    public PermissionDTO save(PermissionDTO dto) throws ServiceException {
        if (dto.getCategory() == null || dto.getCategory().isBlank()) {
            throw new ServiceException("Permission category cannot be empty");
        }
        try {
            Permission saved = permissionDAO.save(DTOMapper.toEntity(dto));
            return DTOMapper.toDTO(saved);
        } catch (SQLException e) {
            throw new ServiceException("Error saving permission", e);
        }
    }
    
    /** 
        * Deletes a permission by its unique identifier. The permission will only be deleted if it is not currently assigned to any users.
        * @param id The unique identifier of the permission to delete.
        * @return true if the permission was successfully deleted, false otherwise (e.g., if the permission is in use or does not exist).
        * @throws ServiceException If an error occurs while deleting the permission from the database, such as SQL exceptions or if the permission is currently assigned to users.
    **/
    @Override
    public boolean delete(int id) throws ServiceException {
        try {
            if (permissionDAO.isInUse(id)) {
                throw new ServiceException("Cannot delete: permission is assigned to at least one user");
            }
            return permissionDAO.delete(id);
        } catch (SQLException e) {
            throw new ServiceException("Error deleting permission with ID: " + id, e);
        }
    }

    /** 
        * Checks if a permission with the specified ID exists in the database.
        * @param id The unique identifier of the permission to check.
        * @return true if the permission exists, false otherwise.
        * @throws ServiceException If an error occurs while checking the permission existence.
    **/
    @Override
    public boolean exists(int id) throws ServiceException {
        try {
            return permissionDAO.exists(id);
        } catch (SQLException e) {
            throw new ServiceException("Error checking permission existence", e);
        }
    }
    
    /** 
        * Counts the total number of permissions in the database.
        * @return The total count of permissions.
        * @throws ServiceException If an error occurs while counting permissions in the database.
    **/
    @Override
    public int count() throws ServiceException {
        try {
            return permissionDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Error counting permissions", e);
        }
    }
}