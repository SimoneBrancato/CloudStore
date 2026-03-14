package com.cloudstore.service.facade;

import com.cloudstore.dao.impl.PermissionDAOImpl;
import com.cloudstore.dao.impl.ProductDAOImpl;
import com.cloudstore.dao.impl.TransactionDAOImpl;
import com.cloudstore.dao.impl.UserDAOImpl;
import com.cloudstore.dao.interfaces.ProductDAO;
import com.cloudstore.dao.interfaces.TransactionDAO;
import com.cloudstore.model.entities.Product;
import com.cloudstore.model.entities.Transaction;
import com.cloudstore.model.dto.*;
import com.cloudstore.service.exception.ServiceException;
import com.cloudstore.service.impl.*;
import com.cloudstore.service.interfaces.*;
import com.cloudstore.service.mapper.DTOMapper;
import com.cloudstore.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class CloudStoreFacade {

    private final PermissionService permissionService;
    private final ProductService productService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final ProductDAO productDAO;
    private final TransactionDAO transactionDAO;
    private final DatabaseConnection dbConnection;

    public CloudStoreFacade() throws ServiceException {
        try {
            PermissionDAOImpl permissionDAO = new PermissionDAOImpl();
            ProductDAOImpl productDAO = new ProductDAOImpl();
            UserDAOImpl userDAO = new UserDAOImpl();
            TransactionDAOImpl transactionDAO = new TransactionDAOImpl();

            this.permissionService = new PermissionServiceImpl(permissionDAO);
            this.productService = new ProductServiceImpl(productDAO);
            this.userService = new UserServiceImpl(userDAO, permissionDAO);
            this.transactionService = new TransactionServiceImpl(transactionDAO);
            this.productDAO = productDAO;
            this.transactionDAO = transactionDAO;
            this.dbConnection = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            throw new ServiceException("Impossible to initialize CloudStoreFacade", e);
        }
    }

    public CloudStoreFacade(
            PermissionService permissionService,
            ProductService productService,
            UserService userService,
            TransactionService transactionService,
            ProductDAO productDAO,
            TransactionDAO transactionDAO,
            DatabaseConnection dbConnection) {
        this.permissionService = permissionService;
        this.productService = productService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.productDAO = productDAO;
        this.transactionDAO = transactionDAO;
        this.dbConnection = dbConnection;
    }

    public Optional<PermissionDTO> findPermissionById(int id) throws ServiceException {
        return permissionService.findById(id);
    }

    public Optional<PermissionDTO> findPermissionByCategory(String category) throws ServiceException {
        return permissionService.findByCategory(category);
    }

    public List<PermissionDTO> getAllPermissions() throws ServiceException {
        return permissionService.findAll();
    }

    public PermissionDTO savePermission(PermissionDTO dto) throws ServiceException {
        return permissionService.save(dto);
    }

    public boolean deletePermission(int id) throws ServiceException {
        return permissionService.delete(id);
    }

    public Optional<ProductDTO> findProductById(int id) throws ServiceException {
        return productService.findById(id);
    }

    public List<ProductDTO> findProductsByName(String name) throws ServiceException {
        return productService.findByName(name);
    }

    public List<ProductDTO> findProductsByCategory(String category) throws ServiceException {
        return productService.findByCategory(category);
    }

    public List<ProductDTO> getAllProducts() throws ServiceException {
        return productService.findAll();
    }

    public ProductDTO saveProduct(ProductDTO dto) throws ServiceException {
        return productService.save(dto);
    }

    public boolean deleteProduct(int id) throws ServiceException {
        return productService.delete(id);
    }

    public boolean updateProductStock(int productId, int newQuantity) throws ServiceException {
        return productService.updateStock(productId, newQuantity);
    }

    public List<ProductDTO> findLowStockProducts(int threshold) throws ServiceException {
        return productService.findLowStockProducts(threshold);
    }

    public Optional<UserDTO> findUserByNickname(String nickname) throws ServiceException {
        return userService.findByNickname(nickname);
    }

    public Optional<UserDTO> findUserByEmail(String email) throws ServiceException {
        return userService.findByEmail(email);
    }

    public List<UserDTO> findUsersByPermission(int permissionId) throws ServiceException {
        return userService.findByPermission(permissionId);
    }

    public List<UserDTO> getAllUsers() throws ServiceException {
        return userService.findAll();
    }

    public UserDTO registerUser(UserDTO dto) throws ServiceException {
        return userService.register(dto);
    }

    public boolean deleteUser(String nickname) throws ServiceException {
        return userService.delete(nickname);
    }

    public boolean updateUserPassword(String nickname, String newPassword) throws ServiceException {
        return userService.updatePassword(nickname, newPassword);
    }

    public boolean updateUserPermission(String nickname, int newPermissionId) throws ServiceException {
        return userService.updatePermission(nickname, newPermissionId);
    }

    public Optional<TransactionDTO> findTransactionById(long id) throws ServiceException {
        return transactionService.findById(id);
    }

    public List<TransactionDTO> findTransactionsByCustomer(String customerName) throws ServiceException {
        return transactionService.findByCustomer(customerName);
    }

    public List<TransactionDTO> findTransactionsByProduct(int productId) throws ServiceException {
        return transactionService.findByProduct(productId);
    }

    public List<TransactionDTO> findTransactionsByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.findByDateRange(start, end);
    }

    public List<TransactionDTO> findTransactionsByPaymentMethod(String paymentMethod) throws ServiceException {
        return transactionService.findByPaymentMethod(paymentMethod);
    }

    public List<TransactionDTO> findTransactionsByCity(String city) throws ServiceException {
        return transactionService.findByCity(city);
    }

    public List<TransactionDTO> getAllTransactions() throws ServiceException {
        return transactionService.findAll();
    }

    public TransactionDTO saveTransaction(TransactionDTO dto) throws ServiceException {
        return transactionService.save(dto);
    }

    public boolean deleteTransaction(long id) throws ServiceException {
        return transactionService.delete(id);
    }

    public double calculateTotalSales(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.calculateTotalSales(start, end);
    }

    public int countTransactionsByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.countByDateRange(start, end);
    }

    public List<TransactionDTO> findRecentTransactions(int limit) throws ServiceException {
        return transactionService.findRecentTransactions(limit);
    }

    public TransactionDTO processOrder(TransactionDTO dto) throws ServiceException {

        if (dto.getDate() == null) {
            dto.setDate(LocalDateTime.now());
        }

        if (dto.getProductDetails() == null) {
            throw new ServiceException("Transaction must specify a product");
        }
        int productId = dto.getProductDetails().getId();

        try (Connection conn = dbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Product product = productDAO.findByIdForUpdate(conn, productId)
                    .orElseThrow(() -> new ServiceException("Product not found with ID: " + productId));

                if (product.stock() < dto.getTotalItems()) {
                    throw new ServiceException(String.format(
                        "Insufficient stock for '%s': available %d, requested %d",
                            product.name(), product.stock(), dto.getTotalItems()));
                }

                Transaction saved = transactionDAO.save(conn, DTOMapper.toEntity(dto));
                boolean stockUpdated = productDAO.updateStock(conn, productId, product.stock() - dto.getTotalItems());
                if (!stockUpdated) {
                    throw new ServiceException("Stock update failed for product ID: " + productId);
                }

                conn.commit();
                return DTOMapper.toDTO(saved);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ServiceException("Error during atomic order processing", e);
        }
    }

    public Map<String, Object> getDashboardStats() throws ServiceException {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts",      productService.count());
        stats.put("totalUsers",         userService.count());
        stats.put("totalTransactions",  transactionService.count());
        stats.put("totalPermissions",   permissionService.count());
        stats.put("monthlySales",       transactionService.calculateTotalSales(startOfMonth, now));
        stats.put("monthlyTransactions",transactionService.countByDateRange(startOfMonth, now));
        stats.put("lowStockProducts",   productService.findLowStockProducts(10).size());

        return stats;
    }

    public Map<String, Object> getUserProfile(String nickname) throws ServiceException {
        Optional<UserDTO> userOpt = userService.findByNickname(nickname);
        if (userOpt.isEmpty()) {
            return null;
        }
        List<TransactionDTO> orders = transactionService.findByCustomer(nickname);
        double totalSpent = orders.stream().mapToDouble(TransactionDTO::getTotalCost).sum();

        Map<String, Object> profile = new HashMap<>();
        profile.put("user",         userOpt.get());
        profile.put("orderHistory", orders);
        profile.put("totalOrders",  orders.size());
        profile.put("totalSpent",   totalSpent);

        return profile;
    }

    public int getFirstAvailablePermissionId() throws ServiceException {
        return permissionService.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("No permission found in database"))
                .getId();
    }

    public boolean permissionExists(int id) throws ServiceException  { return permissionService.exists(id); }
    public boolean productExists(int id) throws ServiceException     { return productService.exists(id); }
    public boolean userExists(String nickname) throws ServiceException{ return userService.exists(nickname); }
    public boolean transactionExists(long id) throws ServiceException { return transactionService.exists(id); }

    public int countPermissions() throws ServiceException  { return permissionService.count(); }
    public int countProducts() throws ServiceException     { return productService.count(); }
    public int countUsers() throws ServiceException        { return userService.count(); }
    public int countTransactions() throws ServiceException { return transactionService.count(); }
}

