package dao.impl;

import dao.interfaces.PermissionDAO;
import model.entities.Permission;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermissionDAOImpl implements PermissionDAO {
    
    private final DatabaseConnection dbConnection; // Database connection instance
    
    // Constructor initializes the database connection.
    public PermissionDAOImpl() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
        * Find a permission by its ID.
        * @param id: The ID of the permission to find.
        * @return An Optional containing the found Permission, or empty if not found. 
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public Optional<Permission> findById(int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM permissions WHERE ID = ?")) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermission(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
        * Find a category by its name.
        * @param category: The name of the category to find.
        * @return An Optional containing the found Permission, or empty if not found. 
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public Optional<Permission> findByCategory(String category) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM permissions WHERE Category = ?")) {
            
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermission(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
        * Find all permissions in the database.
        * @return A list of all permissions.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public List<Permission> findAll() throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM permissions ORDER BY ID")) {
            
            while (rs.next()) {
                permissions.add(mapResultSetToPermission(rs));
            }
        }
        return permissions;
    }
    
    /**
        * Save a permission to the database.
        * @param permission: The permission to save.
        * @return The saved permission with its ID.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public Permission save(Permission permission) throws SQLException {
        if (permission.id() != 0 && exists(permission.id())) {
            return update(permission);
        } else {
            return insert(permission);
        }
    }
    
    /**
        * Insert a new permission into the database.
        * @param permission: The permission to insert.
        * @return The inserted permission with its ID.
        * @throws SQLException If a database access error occurs.
    **/
    private Permission insert(Permission permission) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO permissions (Category) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, permission.category());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creation permission failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Permission(
                        generatedKeys.getInt(1),
                        permission.category()
                    );
                } else {
                    throw new SQLException("Creation permission failed, no generated key obtained.");
                }
            }
        }
    }
    
    /**
        * Update an existing permission in the database.
        * @param permission: The permission to update.
        * @return The updated permission with its ID.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public Permission update(Permission permission) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE permissions SET Category = ? WHERE ID = ?")) {
            
            stmt.setString(1, permission.category());
            stmt.setInt(2, permission.id());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Update permission failed, no rows updated.");
            }
            
            return permission;
        }
    }
    

    /**
        * Delete a permission from the database.
        * @param id: The ID of the permission to delete.
        * @return True if the permission was deleted, false otherwise.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public boolean delete(int id) throws SQLException {
        if (isInUse(id)) {
            throw new SQLException("Impossible to delete: the permission is assigned to users");
        }
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM permissions WHERE ID = ?")) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
        * Check if a permission exists in the database.
        * @param id: The ID of the permission to check.
        * @return True if the permission exists, false otherwise.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public boolean exists(int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM permissions WHERE ID = ?")) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
        * Check if a permission is in use by any user.
        * @param id: The ID of the permission to check.
        * @return True if the permission is in use, false otherwise.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public boolean isInUse(int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE Permission_ID = ?")) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    /**
        * Count the total number of permissions in the database.
        * @return The total count of permissions.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public int count() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM permissions")) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
        * Initialize the database with default permissions if they do not already exist.
        * @throws SQLException If a database access error occurs.
    **/
    public void initializeDefaultPermissions() throws SQLException {
        String[] defaultPermissions = {"ADMIN", "MANAGER", "USER", "GUEST"};
        
        for (String category : defaultPermissions) {
            Optional<Permission> existing = findByCategory(category);
            if (existing.isEmpty()) {
                save(new Permission(0, category));
            }
        }
    }
    
    /**
        * Map a ResultSet to a Permission object.
        * @param rs: The ResultSet to map.
        * @return The mapped Permission object.
        * @throws SQLException If a database access error occurs.
    **/
    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        return new Permission(
            rs.getInt("ID"),
            rs.getString("Category")
        );
    }
}