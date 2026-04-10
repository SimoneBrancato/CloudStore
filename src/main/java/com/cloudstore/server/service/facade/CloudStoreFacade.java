package com.cloudstore.server.service.facade;

import com.cloudstore.server.model.dto.*;
import com.cloudstore.server.model.dto.auth.AuthenticationResult;
import com.cloudstore.server.model.dto.auth.LoginResult;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.*;
import com.cloudstore.server.service.auth.rbac.AccessControl;
import com.cloudstore.server.service.impl.*;
import com.cloudstore.server.model.entities.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class CloudStoreFacade {

    private final PermissionService permissionService;      // Service for managing permissions
    private final ProductService productService;            // Service for managing products
    private final UserService userService;                  // Service for managing users
    private final TransactionService transactionService;    // Service for managing transactions
    private final AuthService authService;                  // Service for handling authentication and authorization
    private final CartService cartService;                  // Service for managing shopping cart operations
    private final DashboardService dashboardService;        // Service for providing dashboard statistics and user profiles
    private final AccessControl accessControl;              // Service for role-based access control

    // Default constructor that initializes all services with their default implementations
    public CloudStoreFacade() throws ServiceException {
        try {
            this.permissionService = new PermissionServiceImpl();
            this.productService = new ProductServiceImpl();
            this.userService = new UserServiceImpl();
            this.transactionService = new TransactionServiceImpl();
            this.authService = new AuthServiceImpl();
            this.cartService = new CartServiceImpl();
            this.dashboardService = new DashboardServiceImpl();
            this.accessControl = new AccessControl();
        } catch (Exception e) {
            throw new ServiceException("Unable to initialize CloudStoreFacade", e);
        }
    }

    /** ROLE BASED METHODS */

    @FunctionalInterface
    public interface RoleBasedInterface<T> {
        T get() throws ServiceException;
    }

    private <T> T adminMethod(RoleBasedInterface<T> action) throws ServiceException {
        this.accessControl.requireRole(Role.ADMIN);
        return action.get();
    }

    private <T> T sellerMethod(RoleBasedInterface<T> action) throws ServiceException {
        this.accessControl.requireRole(Role.SELLER);
        return action.get();
    }

    private <T> T customerMethod(String nickname, RoleBasedInterface<T> action) throws ServiceException {
        AuthenticationResult caller = this.accessControl.requireRole(Role.CUSTOMER);
        requireSelfOrAdmin(caller, nickname);
        return action.get();
    }

    private void requireSelfOrAdmin(AuthenticationResult caller, String nickname) throws ServiceException {
        if (caller.getNickname().equals(nickname))
            return;
        if (accessControl.resolveRole(caller.getRoles()).hasAccessTo(Role.ADMIN))
            return;
        throw new ServiceException("Access denied: you can only access your own resources");
    }

    // -------------------------------------------------------------------------
    // Admin only
    // -------------------------------------------------------------------------

    public Optional<PermissionDTO> findPermissionById(int id) throws ServiceException {
        return adminMethod(() -> permissionService.findById(id));
    }

    public Optional<PermissionDTO> findPermissionByCategory(String category) throws ServiceException {
        return adminMethod(() -> permissionService.findByCategory(category));
    }

    public List<PermissionDTO> getAllPermissions() throws ServiceException {
        return adminMethod(() -> permissionService.findAll());
    }

    public PermissionDTO savePermission(PermissionDTO dto) throws ServiceException {
        return adminMethod(() -> permissionService.save(dto));
    }

    public boolean deletePermission(int id) throws ServiceException {
        return adminMethod(() -> permissionService.delete(id));
    }

    public int countPermissions() throws ServiceException {
        return adminMethod(() -> permissionService.count());
    }

    public ProductDTO saveProduct(ProductDTO dto) throws ServiceException {
        return adminMethod(() -> productService.save(dto));
    }

    public boolean deleteProduct(int id) throws ServiceException {
        return adminMethod(() -> productService.delete(id));
    }

    public boolean updateProductStock(int productId, int newQuantity) throws ServiceException {
        return adminMethod(() -> productService.updateStock(productId, newQuantity));
    }

    public List<ProductDTO> findLowStockProducts(int threshold) throws ServiceException {
        return adminMethod(() -> productService.findLowStockProducts(threshold));
    }

    public int countProducts() throws ServiceException {
        return adminMethod(() -> productService.count());
    }

    public List<UserDTO> findUsersByPermission(int permissionId) throws ServiceException {
        return adminMethod(() -> userService.findByPermission(permissionId));
    }

    public List<UserDTO> getAllUsers() throws ServiceException {
        return adminMethod(() -> userService.findAll());
    }

    public boolean deleteUser(String nickname) throws ServiceException {
        return adminMethod(() -> userService.delete(nickname));
    }

    public boolean updateUserPassword(String nickname, String newPassword) throws ServiceException {
        return adminMethod(() -> userService.updatePassword(nickname, newPassword));
    }

    public boolean updateUserPermission(String nickname, int newPermissionId) throws ServiceException {
        return adminMethod(() -> userService.updatePermission(nickname, newPermissionId));
    }

    public int countUsers() throws ServiceException {
        return adminMethod(() -> userService.count());
    }

    public Optional<TransactionDTO> findTransactionById(long id) throws ServiceException {
        return adminMethod(() -> transactionService.findById(id));
    }

    public List<TransactionDTO> findTransactionsByProduct(int productId) throws ServiceException {
        return adminMethod(() -> transactionService.findByProduct(productId));
    }

    public List<TransactionDTO> findRecentTransactionsByProduct(int productId, int limit) throws ServiceException {
        return adminMethod(() -> transactionService.findRecentByProduct(productId, limit));
    }

    public List<TransactionDTO> findTransactionsByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return adminMethod(() -> transactionService.findByDateRange(start, end));
    }

    public List<TransactionDTO> findTransactionsByPaymentMethod(String paymentMethod) throws ServiceException {
        return adminMethod(() -> transactionService.findByPaymentMethod(paymentMethod));
    }

    public List<TransactionDTO> findTransactionsByCity(String city) throws ServiceException {
        return adminMethod(() -> transactionService.findByCity(city));
    }

    public List<TransactionDTO> getAllTransactions() throws ServiceException {
        return adminMethod(() -> transactionService.findAll());
    }

    public TransactionDTO saveTransaction(TransactionDTO dto) throws ServiceException {
        return adminMethod(() -> transactionService.save(dto));
    }

    public boolean deleteTransaction(long id) throws ServiceException {
        return adminMethod(() -> transactionService.delete(id));
    }

    public double calculateTotalSales(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return adminMethod(() -> transactionService.calculateTotalSales(start, end));
    }

    public int countTransactionsByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return adminMethod(() -> transactionService.countByDateRange(start, end));
    }

    public List<TransactionDTO> findRecentTransactions(int limit) throws ServiceException {
        return adminMethod(() -> transactionService.findRecentTransactions(limit));
    }

    public int countTransactions() throws ServiceException {
        return adminMethod(() -> transactionService.count());
    }

    public Map<String, Object> getDashboardStats() throws ServiceException {
        return adminMethod(() -> dashboardService.getDashboardStats());
    }

    // -------------------------------------------------------------------------
    // PRODUCT
    // -------------------------------------------------------------------------

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

    public List<String> getAllProductCategories() throws ServiceException {
        return productService.findAllCategories();
    }

    public boolean productExists(int id) throws ServiceException {
        return productService.exists(id);
    }

    // -------------------------------------------------------------------------
    // USER — registration public, reads/writes admin, self-service customer
    // -------------------------------------------------------------------------

    public Optional<UserDTO> findUserByNickname(String nickname) throws ServiceException {
        return userService.findByNickname(nickname);
    }

    public Optional<UserDTO> findUserByEmail(String email) throws ServiceException {
        return userService.findByEmail(email);
    }

    public UserDTO registerUser(UserDTO dto) throws ServiceException {
        return userService.register(dto);
    }

    public boolean userExists(String nickname) throws ServiceException {
        return userService.exists(nickname);
    }

    public String resolveCustomerCategory(String customerName) throws ServiceException {
        return userService.resolveCustomerCategory(customerName);
    }

    // -------------------------------------------------------------------------
    // Customer level operations
    // -------------------------------------------------------------------------

    public List<TransactionDTO> findTransactionsByCustomer(String customerName) throws ServiceException {
        return customerMethod(customerName, () -> transactionService.findByCustomer(customerName));
    }

    public Map<String, Object> getCheckoutContext(String customerName, Map<Integer, Integer> items) throws ServiceException {
        return customerMethod(customerName, () -> cartService.getCheckoutContext(customerName, items));
    }

    public TransactionDTO processOrder(String customerName, TransactionDTO dto) throws ServiceException {
        return customerMethod(customerName, () -> cartService.processSingleOrder(dto));
    }

    public Map<String, Object> processCartOrder(String customerName, String paymentMethod,
                                                String city, Map<Integer, Integer> items) throws ServiceException {
        return customerMethod(customerName, () -> cartService.processCartOrder(customerName, paymentMethod, city, items));
    }

    public Map<String, Object> getUserProfile(String nickname) throws ServiceException {
        return customerMethod(nickname, () -> dashboardService.getUserProfile(nickname));
    }

    // -------------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------------

    public int getFirstAvailablePermissionId() throws ServiceException {
        return adminMethod(() -> permissionService.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("No permission found"))
                .getId());
    }

    public boolean permissionExists(int id) throws ServiceException {
        return permissionService.exists(id);
    }

    public boolean transactionExists(long id) throws ServiceException {
        return transactionService.exists(id);
    }

    // -------------------------------------------------------------------------
    // Seller level operations
    // -------------------------------------------------------------------------

    public Map<String, Object> getSellerDashboardStats() throws ServiceException {
        return sellerMethod(() -> dashboardService.getSellerDashboardStats());
    }

    public List<?> getSellerProducts() throws ServiceException {
        return sellerMethod(() -> dashboardService.getSellerProducts());
    }

    public List<Map<String, Object>> getSellerSalesOrders(int limit) throws ServiceException {
        return sellerMethod(() -> dashboardService.getSellerSalesOrders(limit));
    }

    public List<Map<String, Object>> getSellerTopCustomers(int limit) throws ServiceException {
        return sellerMethod(() -> dashboardService.getSellerTopCustomers(limit));
    }

    public boolean updateSellerProductStock(int productId, int newQuantity) throws ServiceException {
        return sellerMethod(() -> productService.updateStock(productId, newQuantity));
    }

    // -------------------------------------------------------------------------
    // AUTH — public
    // -------------------------------------------------------------------------

    public LoginResult authenticateUser(String nickname, String password) throws ServiceException {
        return authService.authenticateUser(nickname, password);
    }
}