package service.facade;

import model.dto.*;
import model.dto.auth.AuthenticationResult;
import model.dto.auth.LoginResult;
import service.exception.ServiceException;
import service.interfaces.*;
import service.impl.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Facade - PUNTO DI INGRESSO UNICO
 * Contiene SOLO chiamate dirette ai service
 * NESSUNA logica di business
 * NESSUN accesso diretto ai DAO
 */
public class CloudStoreFacade {

    private final PermissionService permissionService;
    private final ProductService productService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AuthService authService;
    private final CartService cartService;
    private final DashboardService dashboardService;

    // Costruttore con injection
    public CloudStoreFacade(PermissionService permissionService,
                            ProductService productService,
                            UserService userService,
                            TransactionService transactionService,
                            AuthService authService,
                            CartService cartService,
                            DashboardService dashboardService) {
        this.permissionService = permissionService;
        this.productService = productService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.authService = authService;
        this.cartService = cartService;
        this.dashboardService = dashboardService;
    }

    // Costruttore di default per compatibilità
    public CloudStoreFacade() throws ServiceException {
        try {
            this.permissionService = new PermissionServiceImpl();
            this.productService = new ProductServiceImpl();
            this.userService = new UserServiceImpl();
            this.transactionService = new TransactionServiceImpl();
            this.authService = new AuthServiceImpl();
            this.cartService = new CartServiceImpl();
            this.dashboardService = new DashboardServiceImpl();
        } catch (Exception e) {
            throw new ServiceException("Unable to initialize CloudStoreFacade", e);
        }
    }

    // ==================== PERMISSION ====================
    public Optional<PermissionDTO> findPermissionById(int id) throws ServiceException {
        return permissionService.findById(id);
    }

    public Optional<PermissionDTO> findPermissionById(String token, int id) throws ServiceException {
        return permissionService.findById(id);
    }

    public Optional<PermissionDTO> findPermissionByCategory(String category) throws ServiceException {
        return permissionService.findByCategory(category);
    }

    public Optional<PermissionDTO> findPermissionByCategory(String token, String category) throws ServiceException {
        return permissionService.findByCategory(category);
    }

    public List<PermissionDTO> getAllPermissions() throws ServiceException {
        return permissionService.findAll();
    }

    public List<PermissionDTO> getAllPermissions(String token) throws ServiceException {
        return permissionService.findAll();
    }

    public PermissionDTO savePermission(PermissionDTO dto) throws ServiceException {
        return permissionService.save(dto);
    }

    public PermissionDTO savePermission(String token, PermissionDTO dto) throws ServiceException {
        return permissionService.save(dto);
    }

    public boolean deletePermission(int id) throws ServiceException {
        return permissionService.delete(id);
    }

    public boolean deletePermission(String token, int id) throws ServiceException {
        return permissionService.delete(id);
    }

    public boolean permissionExists(int id) throws ServiceException {
        return permissionService.exists(id);
    }

    public boolean permissionExists(String token, int id) throws ServiceException {
        return permissionService.exists(id);
    }

    public int countPermissions() throws ServiceException {
        return permissionService.count();
    }

    public int countPermissions(String token) throws ServiceException {
        return permissionService.count();
    }

    // ==================== PRODUCT ====================
    public Optional<ProductDTO> findProductById(int id) throws ServiceException {
        return productService.findById(id);
    }

    public Optional<ProductDTO> findProductById(String token, int id) throws ServiceException {
        return productService.findById(id);
    }

    public List<ProductDTO> findProductsByName(String name) throws ServiceException {
        return productService.findByName(name);
    }

    public List<ProductDTO> findProductsByName(String token, String name) throws ServiceException {
        return productService.findByName(name);
    }

    public List<ProductDTO> findProductsByCategory(String category) throws ServiceException {
        return productService.findByCategory(category);
    }

    public List<ProductDTO> findProductsByCategory(String token, String category) throws ServiceException {
        return productService.findByCategory(category);
    }

    public List<ProductDTO> getAllProducts() throws ServiceException {
        return productService.findAll();
    }

    public List<ProductDTO> getAllProducts(String token) throws ServiceException {
        return productService.findAll();
    }

    public List<String> getAllProductCategories() throws ServiceException {
        return productService.findAllCategories();
    }

    public List<String> getAllProductCategories(String token) throws ServiceException {
        return productService.findAllCategories();
    }

    public ProductDTO saveProduct(ProductDTO dto) throws ServiceException {
        return productService.save(dto);
    }

    public ProductDTO saveProduct(String token, ProductDTO dto) throws ServiceException {
        return productService.save(dto);
    }

    public boolean deleteProduct(int id) throws ServiceException {
        return productService.delete(id);
    }

    public boolean deleteProduct(String token, int id) throws ServiceException {
        return productService.delete(id);
    }

    public boolean updateProductStock(int productId, int newQuantity) throws ServiceException {
        return productService.updateStock(productId, newQuantity);
    }

    public boolean updateProductStock(String token, int productId, int newQuantity) throws ServiceException {
        return productService.updateStock(productId, newQuantity);
    }

    public List<ProductDTO> findLowStockProducts(int threshold) throws ServiceException {
        return productService.findLowStockProducts(threshold);
    }

    public List<ProductDTO> findLowStockProducts(String token, int threshold) throws ServiceException {
        return productService.findLowStockProducts(threshold);
    }

    public boolean productExists(int id) throws ServiceException {
        return productService.exists(id);
    }

    public boolean productExists(String token, int id) throws ServiceException {
        return productService.exists(id);
    }

    public int countProducts() throws ServiceException {
        return productService.count();
    }

    public int countProducts(String token) throws ServiceException {
        return productService.count();
    }

    // ==================== USER ====================
    public Optional<UserDTO> findUserByNickname(String nickname) throws ServiceException {
        return userService.findByNickname(nickname);
    }

    public Optional<UserDTO> findUserByNickname(String token, String nickname) throws ServiceException {
        return userService.findByNickname(nickname);
    }

    public Optional<UserDTO> findUserByEmail(String email) throws ServiceException {
        return userService.findByEmail(email);
    }

    public Optional<UserDTO> findUserByEmail(String token, String email) throws ServiceException {
        return userService.findByEmail(email);
    }

    public List<UserDTO> findUsersByPermission(int permissionId) throws ServiceException {
        return userService.findByPermission(permissionId);
    }

    public List<UserDTO> findUsersByPermission(String token, int permissionId) throws ServiceException {
        return userService.findByPermission(permissionId);
    }

    public List<UserDTO> getAllUsers() throws ServiceException {
        return userService.findAll();
    }

    public List<UserDTO> getAllUsers(String token) throws ServiceException {
        return userService.findAll();
    }

    public UserDTO registerUser(UserDTO dto) throws ServiceException {
        return userService.register(dto);
    }

    public UserDTO registerUser(String token, UserDTO dto) throws ServiceException {
        return userService.register(dto);
    }

    public boolean deleteUser(String nickname) throws ServiceException {
        return userService.delete(nickname);
    }

    public boolean deleteUser(String token, String nickname) throws ServiceException {
        return userService.delete(nickname);
    }

    public boolean updateUserPassword(String nickname, String newPassword) throws ServiceException {
        return userService.updatePassword(nickname, newPassword);
    }

    public boolean updateUserPassword(String token, String nickname, String newPassword) throws ServiceException {
        return userService.updatePassword(nickname, newPassword);
    }

    public boolean updateUserPermission(String nickname, int newPermissionId) throws ServiceException {
        return userService.updatePermission(nickname, newPermissionId);
    }

    public boolean updateUserPermission(String token, String nickname, int newPermissionId) throws ServiceException {
        return userService.updatePermission(nickname, newPermissionId);
    }

    public boolean userExists(String nickname) throws ServiceException {
        return userService.exists(nickname);
    }

    public boolean userExists(String token, String nickname) throws ServiceException {
        return userService.exists(nickname);
    }

    public int countUsers() throws ServiceException {
        return userService.count();
    }

    public int countUsers(String token) throws ServiceException {
        return userService.count();
    }

    public String resolveCustomerCategory(String customerName) throws ServiceException {
        return userService.resolveCustomerCategory(customerName);
    }

    public String resolveCustomerCategory(String token, String customerName) throws ServiceException {
        return userService.resolveCustomerCategory(customerName);
    }

    // ==================== AUTH ====================
    public LoginResult authenticateUser(String nickname, String password) throws ServiceException {
        return authService.authenticateUser(nickname, password);
    }

    public AuthenticationResult authenticateByToken(String token) throws ServiceException {
        return authService.authenticateByToken(token);
    }

    public boolean validateToken(String token) throws ServiceException {
        return authService.validateToken(token);
    }

    public void assertAdminAccess(String nickname, String password) throws ServiceException {
        authService.assertAdminAccess(nickname, password);
    }

    public void assertAdminAccess(String token, String nickname, String password) throws ServiceException {
        authService.assertAdminAccess(nickname, password);
    }

    // ==================== TRANSACTION ====================
    public Optional<TransactionDTO> findTransactionById(long id) throws ServiceException {
        return transactionService.findById(id);
    }

    public Optional<TransactionDTO> findTransactionById(String token, long id) throws ServiceException {
        return transactionService.findById(id);
    }

    public List<TransactionDTO> findTransactionsByCustomer(String customerName) throws ServiceException {
        return transactionService.findByCustomer(customerName);
    }

    public List<TransactionDTO> findTransactionsByCustomer(String token, String customerName) throws ServiceException {
        return transactionService.findByCustomer(customerName);
    }

    public List<TransactionDTO> findTransactionsByProduct(int productId) throws ServiceException {
        return transactionService.findByProduct(productId);
    }

    public List<TransactionDTO> findTransactionsByProduct(String token, int productId) throws ServiceException {
        return transactionService.findByProduct(productId);
    }

    public List<TransactionDTO> findRecentTransactionsByProduct(int productId, int limit) throws ServiceException {
        return transactionService.findRecentByProduct(productId, limit);
    }

    public List<TransactionDTO> findRecentTransactionsByProduct(String token, int productId, int limit) throws ServiceException {
        return transactionService.findRecentByProduct(productId, limit);
    }

    public List<TransactionDTO> findTransactionsByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.findByDateRange(start, end);
    }

    public List<TransactionDTO> findTransactionsByDateRange(String token, LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.findByDateRange(start, end);
    }

    public List<TransactionDTO> findTransactionsByPaymentMethod(String paymentMethod) throws ServiceException {
        return transactionService.findByPaymentMethod(paymentMethod);
    }

    public List<TransactionDTO> findTransactionsByPaymentMethod(String token, String paymentMethod) throws ServiceException {
        return transactionService.findByPaymentMethod(paymentMethod);
    }

    public List<TransactionDTO> findTransactionsByCity(String city) throws ServiceException {
        return transactionService.findByCity(city);
    }

    public List<TransactionDTO> findTransactionsByCity(String token, String city) throws ServiceException {
        return transactionService.findByCity(city);
    }

    public List<TransactionDTO> getAllTransactions() throws ServiceException {
        return transactionService.findAll();
    }

    public List<TransactionDTO> getAllTransactions(String token) throws ServiceException {
        return transactionService.findAll();
    }

    public TransactionDTO saveTransaction(TransactionDTO dto) throws ServiceException {
        return transactionService.save(dto);
    }

    public TransactionDTO saveTransaction(String token, TransactionDTO dto) throws ServiceException {
        return transactionService.save(dto);
    }

    public boolean deleteTransaction(long id) throws ServiceException {
        return transactionService.delete(id);
    }

    public boolean deleteTransaction(String token, long id) throws ServiceException {
        return transactionService.delete(id);
    }

    public double calculateTotalSales(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.calculateTotalSales(start, end);
    }

    public double calculateTotalSales(String token, LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.calculateTotalSales(start, end);
    }

    public int countTransactionsByDateRange(LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.countByDateRange(start, end);
    }

    public int countTransactionsByDateRange(String token, LocalDateTime start, LocalDateTime end) throws ServiceException {
        return transactionService.countByDateRange(start, end);
    }

    public List<TransactionDTO> findRecentTransactions(int limit) throws ServiceException {
        return transactionService.findRecentTransactions(limit);
    }

    public List<TransactionDTO> findRecentTransactions(String token, int limit) throws ServiceException {
        return transactionService.findRecentTransactions(limit);
    }

    public boolean transactionExists(long id) throws ServiceException {
        return transactionService.exists(id);
    }

    public boolean transactionExists(String token, long id) throws ServiceException {
        return transactionService.exists(id);
    }

    public int countTransactions() throws ServiceException {
        return transactionService.count();
    }

    public int countTransactions(String token) throws ServiceException {
        return transactionService.count();
    }

    // ==================== CART ====================
    public Map<String, Object> getCheckoutContext(String customerName, Map<Integer, Integer> items) throws ServiceException {
        return cartService.getCheckoutContext(customerName, items);
    }

    public Map<String, Object> getCheckoutContext(String token, String customerName, Map<Integer, Integer> items) throws ServiceException {
        return cartService.getCheckoutContext(customerName, items);
    }

    public TransactionDTO processOrder(TransactionDTO dto) throws ServiceException {
        return cartService.processSingleOrder(dto);
    }

    public TransactionDTO processOrder(String token, TransactionDTO dto) throws ServiceException {
        return cartService.processSingleOrder(dto);
    }

    public Map<String, Object> processCartOrder(String customerName, String paymentMethod, 
                                                String city, Map<Integer, Integer> items) throws ServiceException {
        return cartService.processCartOrder(customerName, paymentMethod, city, items);
    }

    public Map<String, Object> processCartOrder(String token, String customerName, String paymentMethod, 
                                                String city, Map<Integer, Integer> items) throws ServiceException {
        return cartService.processCartOrder(customerName, paymentMethod, city, items);
    }

    // ==================== DASHBOARD ====================
    public Map<String, Object> getDashboardStats() throws ServiceException {
        return dashboardService.getDashboardStats();
    }

    public Map<String, Object> getDashboardStats(String token) throws ServiceException {
        return dashboardService.getDashboardStats();
    }

    public Map<String, Object> getUserProfile(String nickname) throws ServiceException {
        return dashboardService.getUserProfile(nickname);
    }

    public Map<String, Object> getUserProfile(String token, String nickname) throws ServiceException {
        return dashboardService.getUserProfile(nickname);
    }
    
    public int getFirstAvailablePermissionId() throws ServiceException {
        return permissionService.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ServiceException("No permission found"))
                .getId();
    }

    public int getFirstAvailablePermissionId(String token) throws ServiceException {
        return getFirstAvailablePermissionId();
    }
}