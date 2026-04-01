package com.cloudstore.server.service.impl;

import com.cloudstore.server.dao.impl.PermissionDAOImpl;
import com.cloudstore.server.dao.impl.UserDAOImpl;
import com.cloudstore.server.dao.interfaces.PermissionDAO;
import com.cloudstore.server.dao.interfaces.UserDAO;
import com.cloudstore.server.model.dto.UserDTO;
import com.cloudstore.server.model.entities.User;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.UserService;
import com.cloudstore.server.service.mapper.DTOMapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {

    // Regular expression pattern for validating email addresses
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    
    // Minimum password length requirement for user registration and updates
    private static final int MIN_PASSWORD_LENGTH = 8;

    // Dependency on UserDAO to perform database operations related to users
    private final UserDAO userDAO;
    
    // Dependency on PermissionDAO to validate permissions during user registration and updates
    private final PermissionDAO permissionDAO;

    
    /** 
        * Constructor for UserServiceImpl that accepts UserDAO and PermissionDAO instances.
        * @param userDAO The UserDAO instance to use for user-related database operations.
        * @param permissionDAO The PermissionDAO instance to use for permission-related database operations.
    **/
    public UserServiceImpl(UserDAO userDAO, PermissionDAO permissionDAO) {
        this.userDAO = userDAO;
        this.permissionDAO = permissionDAO;
    }

    /** 
        * Default constructor for UserServiceImpl that initializes UserDAO and PermissionDAO with default implementations.
        * @throws ServiceException If initialization fails due to database connection issues or other problems.
    **/
    public UserServiceImpl() throws ServiceException {
        try {
            this.userDAO = new UserDAOImpl();
            this.permissionDAO = new PermissionDAOImpl();
        } catch (SQLException e) {
            throw new ServiceException("Unable to initialize UserService", e);
        }
    }

    /** 
        * Retrieves a user by their nickname.
        * @param nickname The nickname of the user to retrieve.
        * @return An Optional containing the UserDTO if found, or empty if not found.
        * @throws ServiceException If an error occurs while retrieving the user from the database.
    **/
    @Override
    public Optional<UserDTO> findByNickname(String nickname) throws ServiceException {
        try {
            return userDAO.findByNickname(nickname).map(DTOMapper::toDTO);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving user: " + nickname, e);
        }
    }

    /** 
        * Retrieves a user by their email address.
        * @param email The email address of the user to retrieve.
        * @return An Optional containing the UserDTO if found, or empty if not found.
        * @throws ServiceException If an error occurs while retrieving the user from the database.
    **/
    @Override
    public Optional<UserDTO> findByEmail(String email) throws ServiceException {
        try {
            return userDAO.findByEmail(email).map(DTOMapper::toDTO);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving user by email: " + email, e);
        }
    }

    /** 
        * Retrieves users by their permission ID.
        * @param permissionId The ID of the permission to filter users by.
        * @return A list of UserDTOs representing the found users.
        * @throws ServiceException If an error occurs while searching for users by permission.
    **/
    @Override
    public List<UserDTO> findByPermission(int permissionId) throws ServiceException {
        try {
            return userDAO.findByPermission(permissionId).stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching users by permission ID: " + permissionId, e);
        }
    }

    /** 
        * Retrieves all users from the database.
        * @return A list of UserDTOs representing all users.
        * @throws ServiceException If an error occurs while retrieving users from the database.
    **/
    @Override
    public List<UserDTO> findAll() throws ServiceException {
        try {
            return userDAO.findAll().stream()
                    .map(DTOMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving users", e);
        }
    }

    /** 
        * Registers a new user in the system after validating the provided user information.
        * @param dto The UserDTO containing the information of the user to register.
        * @return The registered UserDTO with any additional information (e.g., generated ID).
        * @throws ServiceException If registration fails due to validation errors, database issues, or other problems.
    **/
    @Override
    public UserDTO register(UserDTO dto) throws ServiceException {
        validate(dto);
        try {
            if (userDAO.exists(dto.getNickname())) {
                throw new ServiceException("Nickname already in use: " + dto.getNickname());
            }
            if (userDAO.emailExists(dto.getEmail())) {
                throw new ServiceException("Email already registered: " + dto.getEmail());
            }
            if (dto.getPermission() == null || !permissionDAO.exists(dto.getPermission().getId())) {
                throw new ServiceException("Permission not found with ID: "
                        + (dto.getPermission() != null ? dto.getPermission().getId() : "null"));
            }
            User saved = userDAO.save(DTOMapper.toEntity(dto));
            return DTOMapper.toDTO(saved);
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Error registering user", e);
        }
    }

    /** 
        * Deletes a user from the system based on their nickname.
        * @param nickname The nickname of the user to delete.
        * @return true if the user was successfully deleted, false otherwise.
        * @throws ServiceException If an error occurs while deleting the user from the database.
    **/
    @Override
    public boolean delete(String nickname) throws ServiceException {
        try {
            return userDAO.delete(nickname);
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /** 
        * Updates the password for a user based on their nickname.
        * @param nickname The nickname of the user whose password needs to be updated.
        * @param newPassword The new password for the user.
        * @return true if the password was successfully updated, false otherwise.
        * @throws ServiceException If an error occurs while updating the user's password.
    **/
    @Override
    public boolean updatePassword(String nickname, String newPassword) throws ServiceException {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ServiceException("Password must contain at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        try {
            return userDAO.updatePassword(nickname, newPassword);
        } catch (SQLException e) {
            throw new ServiceException("Error updating password for: " + nickname, e);
        }
    }

    /** 
        * Updates the permission for a user based on their nickname.
        * @param nickname The nickname of the user whose permission needs to be updated.
        * @param permissionId The ID of the new permission to assign to the user.
        * @return true if the permission was successfully updated, false otherwise.
        * @throws ServiceException If an error occurs while updating the user's permission or if the specified permission does not exist.
    **/
    @Override
    public boolean updatePermission(String nickname, int permissionId) throws ServiceException {
        try {
            return userDAO.updatePermission(nickname, permissionId);
        } catch (SQLException e) {
            throw new ServiceException("Error updating permission for: " + nickname, e);
        }
    }

    /** 
        * Checks if a user with the specified nickname exists in the database.
        * @param nickname The nickname of the user to check for existence.
        * @return true if the user exists, false otherwise.
        * @throws ServiceException If an error occurs while checking the user's existence in the database.
    **/
    @Override
    public boolean exists(String nickname) throws ServiceException {
        try {
            return userDAO.exists(nickname);
        } catch (SQLException e) {
            throw new ServiceException("Error checking user existence", e);
        }
    }

    /** 
        * Counts the total number of users in the database.
        * @return The total count of users.
        * @throws ServiceException If an error occurs while counting users in the database.
    **/
    @Override
    public int count() throws ServiceException {
        try {
            return userDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Error counting users", e);
        }
    }
    
    /** 
        * Counts the number of users with a specific permission ID.
        * @param permissionId The ID of the permission to filter users by.
        * @return The count of users with the specified permission ID.
        * @throws ServiceException If an error occurs while counting users by permission in the database.
    **/
    @Override
    public String resolveCustomerCategory(String customerName) throws ServiceException {
        try {
            Optional<UserDTO> userOpt = findByNickname(customerName);
            if (userOpt.isPresent()) {
                UserDTO user = userOpt.get();
                if (user.getPermission() != null && user.getPermission().getCategory() != null
                        && !user.getPermission().getCategory().isBlank()) {
                    return user.getPermission().getCategory();
                }
            }
            return "Customer";
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error resolving customer category", e);
        }
    }
    
    /** 
        * Validates the fields of a UserDTO before registering it in the system.
        * @param dto The UserDTO to validate.
        * @throws ServiceException If any validation rules are violated, such as empty nickname, invalid email format, or weak password.
    **/
    private void validate(UserDTO dto) throws ServiceException {
        if (dto.getNickname() == null || dto.getNickname().isBlank()) {
            throw new ServiceException("Nickname cannot be empty");
        }
        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail()).matches()) {
            throw new ServiceException("Invalid email format: " + dto.getEmail());
        }
        if (dto.getPassword() == null || dto.getPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new ServiceException("Password must contain at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }
}