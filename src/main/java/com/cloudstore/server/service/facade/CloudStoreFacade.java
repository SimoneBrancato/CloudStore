package service.facade;

import model.dto.*;
import model.dto.auth.AuthenticationResult;
import model.dto.auth.LoginResult;
import service.exception.ServiceException;
import service.interfaces.*;
import service.auth.rbac.AccessControl;
import service.impl.*;
import model.entities.Role;

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
            this.accessControl = new AccessControl(authService, userService);
        } catch (Exception e) {
            throw new ServiceException("Unable to initialize CloudStoreFacade", e);
        }
    }

    /** ROLE BASED METHODS */

    @FunctionalInterface
    public interface RoleBasedInterface<T> {
        T get() throws ServiceException;
    }

    private <T> T adminMethod(String token, RoleBasedInterface<T> action) throws ServiceException {
        this.accessControl.requireRole(token, Role.ADMIN);
        return action.get();
    }

    private <T> T sellerMethod(String token, RoleBasedInterface<T> action) throws ServiceException {
        this.accessControl.requireRole(token, Role.SELLER);
        return action.get();
    }

    private <T> T customerMethod(String token, String nickname, RoleBasedInterface<T> action) throws ServiceException {
        UserDTO caller = this.accessControl.requireRole(token, Role.CUSTOMER);
        requireSelfOrAdmin(caller, nickname);
        return action.get();
    }

    private void requireSelfOrAdmin(UserDTO caller, String nickname) throws ServiceException {
        if (caller.getNickname().equals(nickname))
            return;
        if (accessControl.resolveRole(caller).hasAccessTo(Role.ADMIN))
            return;
        throw new ServiceException("Access denied: you can only access your own resources");
    }

    // -------------------------------------------------------------------------
    // Admin only
    // -------------------------------------------------------------------------

    public Optional<PermissionDTO> findPermissionById(String token, int id) throws ServiceException {
        return adminMethod(token, () -> permissionService.findById(id));
    }

    public Optional<PermissionDTO> findPermissionByCategory(String token, String category) throws ServiceException {
        return adminMethod(token, () -> permissionService.findByCategory(category));
    }

    public List<PermissionDTO> getAllPermissions(String token) throws ServiceException {
        return adminMethod(token, () -> permissionService.findAll());
    }

    public PermissionDTO savePermission(String token, PermissionDTO dto) throws ServiceException {
        return adminMethod(token, () -> permissionService.save(dto));
    }

    public boolean deletePermission(String token, int id) throws ServiceException {
        return adminMethod(token, () -> permissionService.delete(id));
    }

    public int countPermissions(String token) throws ServiceException {
        return adminMethod(token, () -> permissionService.count());
    }

    public ProductDTO saveProduct(String token, ProductDTO dto) throws ServiceException {
        return adminMethod(token, () -> productService.save(dto));
    }

    public boolean deleteProduct(String token, int id) throws ServiceException {
        return adminMethod(token, () -> productService.delete(id));
    }

    public boolean updateProductStock(String token, int productId, int newQuantity) throws ServiceException {
        return adminMethod(token, () -> productService.updateStock(productId, newQuantity));
    }

    public List<ProductDTO> findLowStockProducts(String token, int threshold) throws ServiceException {
        return adminMethod(token, () -> productService.findLowStockProducts(threshold));
    }

    public int countProducts(String token) throws ServiceException {
        return adminMethod(token, () -> productService.count());
    }

    public List<UserDTO> findUsersByPermission(String token, int permissionId) throws ServiceException {
        return adminMethod(token, () -> userService.findByPermission(permissionId));
    }

    public List<UserDTO> getAllUsers(String token) throws ServiceException {
        return adminMethod(token, () -> userService.findAll());
    }

    public boolean deleteUser(String token, String nickname) throws ServiceException {
        return adminMethod(token, () -> userService.delete(nickname));
    }

    public boolean updateUserPassword(String token, String nickname, String newPassword) throws ServiceException {
        return adminMethod(token, () -> userService.updatePassword(nickname, newPassword));
    }

    public boolean updateUserPermission(String token, String nickname, int newPermissionId) throws ServiceException {
        return adminMethod(token, () -> userService.updatePermission(nickname, newPermissionId));
    }

    public int countUsers(String token) throws ServiceException {
        return adminMethod(token, () -> userService.count());
    }

    public Optional<TransactionDTO> findTransactionById(String token, long id) throws ServiceException {
        return adminMethod(token, () -> transactionService.findById(id));
    }

    public List<TransactionDTO> findTransactionsByProduct(String token, int productId) throws ServiceException {
        return adminMethod(token, () -> transactionService.findByProduct(productId));
    }

    public List<TransactionDTO> findRecentTransactionsByProduct(String token, int productId, int limit) throws ServiceException {
        return adminMethod(token, () -> transactionService.findRecentByProduct(productId, limit));
    }

    public List<TransactionDTO> findTransactionsByDateRange(String token, LocalDateTime start, LocalDateTime end) throws ServiceException {
        return adminMethod(token, () -> transactionService.findByDateRange(start, end));
    }

    public List<TransactionDTO> findTransactionsByPaymentMethod(String token, String paymentMethod) throws ServiceException {
        return adminMethod(token, () -> transactionService.findByPaymentMethod(paymentMethod));
    }

    public List<TransactionDTO> findTransactionsByCity(String token, String city) throws ServiceException {
        return adminMethod(token, () -> transactionService.findByCity(city));
    }

    public List<TransactionDTO> getAllTransactions(String token) throws ServiceException {
        return adminMethod(token, () -> transactionService.findAll());
    }

    public TransactionDTO saveTransaction(String token, TransactionDTO dto) throws ServiceException {
        return adminMethod(token, () -> transactionService.save(dto));
    }

    public boolean deleteTransaction(String token, long id) throws ServiceException {
        return adminMethod(token, () -> transactionService.delete(id));
    }

    public double calculateTotalSales(String token, LocalDateTime start, LocalDateTime end) throws ServiceException {
        return adminMethod(token, () -> transactionService.calculateTotalSales(start, end));
    }

    public int countTransactionsByDateRange(String token, LocalDateTime start, LocalDateTime end) throws ServiceException {
        return adminMethod(token, () -> transactionService.countByDateRange(start, end));
    }

    public List<TransactionDTO> findRecentTransactions(String token, int limit) throws ServiceException {
        return adminMethod(token, () -> transactionService.findRecentTransactions(limit));
    }

    public int countTransactions(String token) throws ServiceException {
        return adminMethod(token, () -> transactionService.count());
    }

    public Map<String, Object> getDashboardStats(String token) throws ServiceException {
        return adminMethod(token, () -> dashboardService.getDashboardStats());
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

    public List<TransactionDTO> findTransactionsByCustomer(String token, String customerName) throws ServiceException {
        return customerMethod(token, customerName, () -> transactionService.findByCustomer(customerName));
    }

    public Map<String, Object> getCheckoutContext(String token, String customerName, Map<Integer, Integer> items) throws ServiceException {
        return customerMethod(token, customerName, () -> cartService.getCheckoutContext(customerName, items));
    }

    public TransactionDTO processOrder(String token, String customerName, TransactionDTO dto) throws ServiceException {
        return customerMethod(token, customerName, () -> cartService.processSingleOrder(dto));
    }

    public Map<String, Object> processCartOrder(String token, String customerName, String paymentMethod,
                                                String city, Map<Integer, Integer> items) throws ServiceException {
        return customerMethod(token, customerName, () -> cartService.processCartOrder(customerName, paymentMethod, city, items));
    }

    public Map<String, Object> getUserProfile(String token, String nickname) throws ServiceException {
        return customerMethod(token, nickname, () -> dashboardService.getUserProfile(nickname));
    }

    // -------------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------------

    public int getFirstAvailablePermissionId(String token) throws ServiceException {
        return adminMethod(token, () -> permissionService.findAll().stream()
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
    // AUTH — public
    // -------------------------------------------------------------------------

    public LoginResult authenticateUser(String nickname, String password) throws ServiceException {
        return authService.authenticateUser(nickname, password);
    }

    public AuthenticationResult getSessionFromToken(String token) throws ServiceException {
        return authService.getSessionFromToken(token);
    }
}