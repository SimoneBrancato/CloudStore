package dao.impl;

import dao.interfaces.ProductDAO;
import model.entities.Product;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {
    
    private final DatabaseConnection dbConnection;  // Database connection instance
    
    // Constructor initializes the database connection.
    public ProductDAOImpl() throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
        * Find a product by its ID.
        * @param id: The ID of the product to find.
        * @return An Optional containing the found Product, or empty if not found. 
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public Optional<Product> findById(int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement( "SELECT * FROM products WHERE Product_Id = ?")) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
        * Find a product by its ID for update (with row locking).
        * @param conn: The database connection to use for the query.
        * @param id: The ID of the product to find.
        * @return An Optional containing the found Product, or empty if not found. 
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public Optional<Product> findByIdForUpdate(Connection conn, int id) throws SQLException {
        String selectForUpdate = "SELECT * FROM products WHERE Product_Id = ?" + " FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(selectForUpdate)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        return Optional.empty();
    }
    
    /**
        * Find a product by its name.
        * @param name: The name of the product to find.
        * @return A list of products matching the name.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public List<Product> findByName(String name) throws SQLException {
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE Product_Name LIKE ?")) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    /**
        * Find categories by their name.
        * @param category: The name of the category to find.
        * @return A list of products matching the category.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public List<Product> findByCategory(String category) throws SQLException {
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE Category = ?")) {
            
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    /**
        * Find all products in the database.
        * @return A list of all products.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products ORDER BY Product_Id")) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }
    
    /**
        * Save a product to the database. 
        * @param product: The product to save.
        * @return The saved product.
        * @throws SQLException If a database access error occurs or if the save operation fails.
    **/
    @Override
    public Product save(Product product) throws SQLException {
        if (exists(product.id())) {
            return update(product);
        } else {
            return insert(product);
        }
    }
    
    /**
        * Insert a new product into the database. 
        * @param product: The product to insert.
        * @return The inserted product with the generated ID.
        * @throws SQLException If a database access error occurs or if the insertion fails.
    **/
    private Product insert(Product product) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO products (Product_Name, Category, Price, Stock_Quantity) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.name());
            stmt.setString(2, product.category());
            stmt.setDouble(3, product.price());
            stmt.setInt(4, product.stock());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creation product failed, no rows inserted.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Product(
                        generatedKeys.getInt(1),
                        product.name(),
                        product.category(),
                        product.price(),
                        product.stock()
                    );
                } else {
                    throw new SQLException("Creation product failed, no generated key obtained.");
                }
            }
        }
    }
    
    /**
        * Update an existing product in the database.
        * @param product: The product to update.
        * @return The updated product.
        * @throws SQLException If a database access error occurs or if the insertion fails.
    **/
    @Override
    public Product update(Product product) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE products SET Product_Name = ?, Category = ?, Price = ?, Stock_Quantity = ? WHERE Product_Id = ?")) {
            
            stmt.setString(1, product.name());
            stmt.setString(2, product.category());
            stmt.setDouble(3, product.price());
            stmt.setInt(4, product.stock());
            stmt.setInt(5, product.id());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Update product failed, no rows updated.");
            }
            
            return product;
        }
    }
    
    /**
        * Delete a product from the database.
        * @param id: The ID of the product to delete.
        * @return true if the product was deleted, false otherwise.
        * @throws SQLException If a database access error occurs or if the deletion fails.
    **/
    @Override
    public boolean delete(int id) throws SQLException {
        String checkTransactions = "SELECT COUNT(*) FROM transactions WHERE Product_Id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkTransactions)) {
            
            checkStmt.setInt(1, id);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Impossible to delete: there are transactions linked to this product");
                }
            }
        }
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE Product_Id = ?")) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
        * Check if a product exists in the database by its ID.
        * @param id: The ID of the product to check.
        * @return true if the product exists, false otherwise.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public boolean exists(int id) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM products WHERE Product_Id = ?")) {
            
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
        * Update the stock quantity of a product.
        * @param productId: The ID of the product to update.
        * @param newQuantity: The new stock quantity to set.
        * @return true if the stock was updated, false otherwise.
        * @throws SQLException If a database access error occurs or if the update fails.
    **/
    @Override
    public boolean updateStock(int productId, int newQuantity) throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE products SET Stock_Quantity = ? WHERE Product_Id = ?")) {
            
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
        * Update the stock quantity of a product using an existing database connection (for transactional operations).
        * @param conn: The database connection to use for the update.
        * @param productId: The ID of the product to update.
        * @param newQuantity: The new stock quantity to set.
        * @return true if the stock was updated, false otherwise.
        * @throws SQLException If a database access error occurs or if the update fails.
    **/
    @Override
    public boolean updateStock(Connection conn, int productId, int newQuantity) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE products SET Stock_Quantity = ? WHERE Product_Id = ?")) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
        * Find products that are low in stock (below a certain threshold).
        * @param threshold: The stock quantity threshold to compare against.
        * @return A list of products that have stock quantity less than or equal to the threshold.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public List<Product> findLowStockProducts(int threshold) throws SQLException {
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE Stock_Quantity <= ?")) {
            
            stmt.setInt(1, threshold);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    /**
        * Count the total number of products in the database.
        * @return The total count of products.
        * @throws SQLException If a database access error occurs.
    **/
    @Override
    public int count() throws SQLException {
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
        * Map a ResultSet row to a Product object.
        * @param rs: The ResultSet to map.
        * @return A Product object initialized with the values from the ResultSet.
        * @throws SQLException If a database access error occurs.
    **/
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("Product_Id"),
            rs.getString("Product_Name"),
            rs.getString("Category"),
            rs.getDouble("Price"),
            rs.getInt("Stock_Quantity")
        );
    }
}