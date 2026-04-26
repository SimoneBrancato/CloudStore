package com.cloudstore.server.service.impl;

import com.cloudstore.server.dao.impl.PermissionDAOImpl;
import com.cloudstore.server.dao.impl.UserDAOImpl;
import com.cloudstore.server.dao.interfaces.PermissionDAO;
import com.cloudstore.server.dao.interfaces.UserDAO;
import com.cloudstore.server.model.entities.User;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.exception.ValidationException;
import com.cloudstore.server.service.interfaces.UserService;
import com.cloudstore.server.service.auth.PasswordHasher;

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
    public Optional<User> findByNickname(String nickname) throws ServiceException {
        try {
            return userDAO.findByNickname(nickname);
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
    public Optional<User> findByEmail(String email) throws ServiceException {
        try {
            return userDAO.findByEmail(email);
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
    public List<User> findByPermission(int permissionId) throws ServiceException {
        try {
            return userDAO.findByPermission(permissionId).stream()
                    
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
    public List<User> findAll() throws ServiceException {
        try {
            return userDAO.findAll().stream()
                    
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
    public User register(User user) throws ServiceException {
        validate(user);
        try {
            if (userDAO.exists(user.nickname())) {
                throw new ValidationException("Nickname already in use: " + user.nickname());
            }
            if (userDAO.emailExists(user.email())) {
                throw new ValidationException("Email already registered: " + user.email());
            }
            if (user.PermissionID() == null || !permissionDAO.exists(user.PermissionID().id())) {
                throw new ServiceException("Permission not found with ID: "
                        + (user.PermissionID() != null ? user.PermissionID().id() : "null"));
            }
            User userToSave = new User(
                    user.nickname(),
                    user.name(),
                    user.surname(),
                    user.email(),
                    PasswordHasher.hash(user.password()),
                    user.PermissionID()
            );
            return userDAO.save(userToSave);
            
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
            throw new ValidationException("Password must contain at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        try {
            return userDAO.updatePassword(nickname, PasswordHasher.hash(newPassword));
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
            Optional<User> userOpt = findByNickname(customerName);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.PermissionID() != null && user.PermissionID().category() != null
                        && !user.PermissionID().category().isBlank()) {
                    return user.PermissionID().category();
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
    private void validate(User user) throws ServiceException {
        if (user.nickname() == null || user.nickname().isBlank()) {
            throw new ValidationException("Nickname cannot be empty");
        }
        if (user.email() == null || !EMAIL_PATTERN.matcher(user.email()).matches()) {
            throw new ValidationException("Invalid email format: " + user.email());
        }
        if (user.password() == null || user.password().length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must contain at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }
}