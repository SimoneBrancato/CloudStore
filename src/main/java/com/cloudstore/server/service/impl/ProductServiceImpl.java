package com.cloudstore.server.service.impl;

import com.cloudstore.server.dao.impl.ProductDAOImpl;
import com.cloudstore.server.dao.interfaces.ProductDAO;
import com.cloudstore.server.model.entities.Product;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.ProductService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService {

    // Dependency on ProductDAO to perform database operations related to products
    private final ProductDAO productDAO;

    /** 
        * Constructor for ProductServiceImpl that accepts a ProductDAO instance.
        * @param productDAO The ProductDAO instance to use for database operations.
    **/
    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    /** 
        * Default constructor for ProductServiceImpl that initializes the ProductDAO with a default implementation.
        * @throws ServiceException If initialization fails due to database connection issues or other problems.
    **/
    public ProductServiceImpl() throws ServiceException {
        try {
            this.productDAO = new ProductDAOImpl();
        } catch (SQLException e) {
            throw new ServiceException("Unable to initialize ProductService", e);
        }
    }

    /** 
        * Retrieves a product by its unique identifier.
        * @param id The unique identifier of the product to retrieve.
        * @return An Optional containing the ProductDTO if found, or empty if not found.
        * @throws ServiceException If an error occurs while retrieving the product from the database.
    **/
    @Override
    public Optional<Product> findById(int id) throws ServiceException {
        try {
            return productDAO.findById(id);
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving product with ID: " + id, e);
        }
    }

    /** 
        * Retrieves products by their name.
        * @param name The name of the products to retrieve.
        * @return A list of ProductDTOs representing the found products.
        * @throws ServiceException If an error occurs while searching for products.
    **/
    @Override
    public List<Product> findByName(String name) throws ServiceException {
        try {
            return productDAO.findByName(name).stream()
                    
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching products by name: " + name, e);
        }
    }

    /** 
        * Retrieves products by their category.
        * @param category The category of the products to retrieve.
        * @return A list of ProductDTOs representing the found products.
        * @throws ServiceException If an error occurs while searching for products.
    **/
    @Override
    public List<Product> findByCategory(String category) throws ServiceException {
        try {
            return productDAO.findByCategory(category).stream()
                    
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching products by category: " + category, e);
        }
    }

    /** 
        * Retrieves all products from the database.
        * @return A list of ProductDTOs representing all products.
        * @throws ServiceException If an error occurs while retrieving products from the database.
    **/
    @Override
    public List<Product> findAll() throws ServiceException {
        try {
            return productDAO.findAll().stream()
                    
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving products", e);
        }
    }

    /** 
        * Retrieves a list of all unique product categories available in the database.
        * @return A list of unique product category names.
        * @throws ServiceException If an error occurs while retrieving product categories from the database.
    **/
    @Override
    public List<String> findAllCategories() throws ServiceException {
        try {
            return productDAO.findAll().stream()
                    .map(Product::category)
                    .filter(category -> category != null && !category.isBlank())
                    .map(String::trim)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error retrieving product categories", e);
        }
    }

    /** 
        * Saves a product to the database. If the product already exists, it will be updated; otherwise, a new product will be created.
        * @param dto The ProductDTO containing the product data to save.
        * @return The saved ProductDTO with any generated fields (e.g., ID) populated.
        * @throws ServiceException If an error occurs while saving the product to the database, such as validation errors or SQL exceptions.
    **/
    @Override
    public Product save(Product product) throws ServiceException {
        validate(product);
        try {
            return productDAO.save(product);
        } catch (SQLException e) {
            throw new ServiceException("Error saving product", e);
        }
    }

    /** 
        * Deletes a product by its unique identifier.
        * @param id The unique identifier of the product to delete.
        * @return true if the product was successfully deleted, false otherwise (e.g., if the product does not exist).
        * @throws ServiceException If an error occurs while deleting the product from the database, such as SQL exceptions.
    **/
    @Override
    public boolean delete(int id) throws ServiceException {
        try {
            return productDAO.delete(id);
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /** 
        * Updates the stock quantity of a product.
        * @param productId The unique identifier of the product to update.
        * @param newQuantity The new stock quantity to set for the product.
        * @return true if the stock was successfully updated, false otherwise (e.g., if the product does not exist).
        * @throws ServiceException If an error occurs while updating the product stock in the database, such as SQL exceptions or validation errors.
    **/
    @Override
    public boolean updateStock(int productId, int newQuantity) throws ServiceException {
        if (newQuantity < 0) {
            throw new ServiceException("Stock quantity cannot be negative");
        }
        try {
            return productDAO.updateStock(productId, newQuantity);
        } catch (SQLException e) {
            throw new ServiceException("Error updating stock for product ID: " + productId, e);
        }
    }

    /** 
        * Retrieves a list of products that have stock quantities below a specified threshold.
        * @param threshold The stock quantity threshold to compare against.
        * @return A list of ProductDTOs representing the products with low stock.
        * @throws ServiceException If an error occurs while retrieving low-stock products from the database, such as SQL exceptions or validation errors.
    **/
    @Override
    public List<Product> findLowStockProducts(int threshold) throws ServiceException {
        if (threshold < 0) {
            throw new ServiceException("Stock threshold cannot be negative");
        }
        try {
            return productDAO.findLowStockProducts(threshold).stream()
                    
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new ServiceException("Error searching low-stock products", e);
        }
    }

    /** 
        * Checks if a product with the specified ID exists in the database.
        * @param id The unique identifier of the product to check.
        * @return true if the product exists, false otherwise.
        * @throws ServiceException If an error occurs while checking product existence in the database, such as SQL exceptions.
    **/
    @Override
    public boolean exists(int id) throws ServiceException {
        try {
            return productDAO.exists(id);
        } catch (SQLException e) {
            throw new ServiceException("Error checking product existence", e);
        }
    }

    /** 
        * Counts the total number of products in the database.
        * @return The total count of products.
        * @throws ServiceException If an error occurs while counting products in the database, such as SQL exceptions.
    **/
    @Override
    public int count() throws ServiceException {
        try {
            return productDAO.count();
        } catch (SQLException e) {
            throw new ServiceException("Error counting products", e);
        }
    }

    /** 
        * Validates the fields of a ProductDTO before saving it to the database.
        * @param dto The ProductDTO to validate.
        * @throws ServiceException If any validation rules are violated, such as empty name, negative price, or negative stock.
    **/
    private void validate(Product product) throws ServiceException {
        if (product.name() == null || product.name().isBlank()) {
            throw new ServiceException("Product name cannot be empty");
        }
        if (product.price() < 0) {
            throw new ServiceException("Product price cannot be negative");
        }
        if (product.stock() < 0) {
            throw new ServiceException("Product stock cannot be negative");
        }
    }
}