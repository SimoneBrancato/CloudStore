/*
package com.cloudstore.service.facade;

import com.cloudstore.dao.impl.PermissionDAOImpl;
import com.cloudstore.dao.impl.ProductDAOImpl;
import com.cloudstore.dao.impl.TransactionDAOImpl;
import com.cloudstore.dao.impl.UserDAOImpl;
import com.cloudstore.dao.interfaces.ProductDAO;
import com.cloudstore.dao.interfaces.TransactionDAO;
import com.cloudstore.model.entities.Product;
import com.cloudstore.model.entities.Role;
import com.cloudstore.model.entities.Transaction;
import com.cloudstore.model.dto.*;
import com.cloudstore.model.dto.auth.LoginResult;
import com.cloudstore.service.auth.rbac.AccessControl;
import com.cloudstore.service.exception.ServiceException;
import com.cloudstore.service.impl.*;
import com.cloudstore.service.interfaces.*;
import com.cloudstore.service.mapper.DTOMapper;
import com.cloudstore.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CloudStoreFacadeOld {

    private final PermissionService permissionService;
    private final ProductService productService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AuthService authService;
    private final ProductDAO productDAO;
    private final TransactionDAO transactionDAO;
    private final DatabaseConnection dbConnection;
    private final AccessControl accessControl;

    public CloudStoreFacadeOld() throws ServiceException {
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
            this.authService = new AuthServiceImpl();
            this.accessControl = new AccessControl(authService, userService);
        } catch (SQLException e) {
            throw new ServiceException("Impossible to initialize CloudStoreFacade", e);
        }
    }

    public CloudStoreFacadeOld(
            PermissionService permissionService,
            ProductService productService,
            UserService userService,
            TransactionService transactionService,
            ProductDAO productDAO,
            TransactionDAO transactionDAO,
            DatabaseConnection dbConnection,
            AuthService authService,
            AccessControl accessControl) {
        this.permissionService = permissionService;
        this.productService = productService;
        this.userService = userService;
        this.transactionService = transactionService;
        this.productDAO = productDAO;
        this.transactionDAO = transactionDAO;
        this.dbConnection = dbConnection;
        this.authService = authService;
        this.accessControl = accessControl;
    }

    /** TEMPLATE METHODS 

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

    /** BRIDGE UTILITY METHODS

    protected List<ProductDTO> findProductsByCategory(String category) throws ServiceException {
        return productService.findByCategory(category);
    }

    protected List<ProductDTO> getAllProducts() throws ServiceException {
        return productService.findAll();
    }

    protected List<String> getProductCategories() throws ServiceException {
        return productService.findAll().stream()
                .map(ProductDTO::getDescription)
                .filter(category -> category != null && !category.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    protected LoginResult authenticateUser(String nickname, String password) throws ServiceException {
        return authService.authenticateUser(nickname, password);
    }

    protected Map<String, Object> getCustomerCheckoutContext(String token, String customerName,
            Map<Integer, Integer> items) throws ServiceException {
        if (customerName == null || customerName.isBlank()) {
            throw new ServiceException("Customer name cannot be empty");
        }
        UserDTO authenticatedUser = this.accessControl.requireRole(token, Role.CUSTOMER);
        if (!authenticatedUser.getNickname().equals(customerName)) {
            throw new ServiceException(authenticatedUser.getNickname()
                    + " is not authorized to access checkout context for " + customerName);
        }

        String customerCategory = resolveCustomerCategory(customerName);
        int sampleWindow = 5;
        Map<String, Object> discountContext = resolveDiscountForCartItems(items, sampleWindow);
        float discount = (float) discountContext.get("discount");
        int sampleSize = (int) discountContext.get("sampleSize");
        String discountSource = (String) discountContext.get("discountSource");

        Map<String, Object> context = new HashMap<>();
        context.put("customerName", customerName);
        context.put("customerCategory", customerCategory);
        context.put("discount", discount);
        context.put("discountApplied", discount > 0 ? 1 : 0);
        context.put("discountSource", discountSource);
        context.put("sampleSize", sampleSize);
        context.put("sampleWindow", sampleWindow);
        return context;
    }

    protected UserDTO registerUser(UserDTO dto) throws ServiceException {
        return userService.register(dto);
    }

    public List<TransactionDTO> findTransactionsByCustomer(String customerName) throws ServiceException {
        return transactionService.findByCustomer(customerName);
    }

    /** CUSTOMER LEVEL OPERATIONS 

    protected TransactionDTO processOrder(String token, TransactionDTO dto) throws ServiceException {
        return customerMethod(token, dto.getCustomerName(), () -> {
            if (dto.getDate() == null) {
                dto.setDate(LocalDateTime.now());
            }

            if (dto.getProductDetails() == null) {
                throw new ServiceException("Transaction must specify a product");
            }
            if (dto.getTotalItems() <= 0) {
                throw new ServiceException("Number of items must be greater than zero");
            }
            int productId = dto.getProductDetails().getId();

            float normalizedDiscount = Math.max(0.0f, Math.min(dto.getDiscount(), 1.0f));
            dto.setDiscount(normalizedDiscount);
            dto.setDiscountApplied(normalizedDiscount > 0 ? 1 : 0);

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

                    double grossTotal = product.price() * dto.getTotalItems();
                    double netTotal = grossTotal * (1 - normalizedDiscount);
                    dto.setProduct(product.name());
                    dto.setTotalCost(netTotal);

                    Transaction saved = transactionDAO.save(conn, DTOMapper.toEntity(dto));
                    boolean stockUpdated = productDAO.updateStock(conn, productId,
                            product.stock() - dto.getTotalItems());
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
        });
    }

    protected Map<String, Object> processCartOrder(
            String token,
            String customerName,
            String paymentMethod,
            String city,
            Map<Integer, Integer> items) throws ServiceException {

        return customerMethod(token, customerName, () -> {
            if (customerName == null || customerName.isBlank()) {
                throw new ServiceException("Customer name cannot be empty");
            }

            if (items == null || items.isEmpty()) {
                throw new ServiceException("Cart is empty");
            }

            String customerCategory = resolveCustomerCategory(customerName);
            Map<String, Object> discountContext = resolveDiscountForCartItems(items, 5);
            float normalizedDiscount = (float) discountContext.get("discount");
            LocalDateTime now = LocalDateTime.now();

            List<Map.Entry<Integer, Integer>> sortedItems = new ArrayList<>(items.entrySet());
            sortedItems.sort(Comparator.comparingInt(Map.Entry::getKey));

            try (Connection conn = dbConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    List<TransactionDTO> createdTransactions = new ArrayList<>();
                    double cartTotal = 0.0;
                    int totalItems = 0;

                    for (Map.Entry<Integer, Integer> entry : sortedItems) {
                        int productId = entry.getKey();
                        int quantity = entry.getValue();

                        if (quantity <= 0) {
                            throw new ServiceException(
                                    "Quantity must be greater than zero for product ID: " + productId);
                        }

                        Product product = productDAO.findByIdForUpdate(conn, productId)
                                .orElseThrow(() -> new ServiceException("Product not found with ID: " + productId));

                        if (product.stock() < quantity) {
                            throw new ServiceException(String.format(
                                    "Insufficient stock for '%s': available %d, requested %d",
                                    product.name(), product.stock(), quantity));
                        }

                        double lineTotal = product.price() * quantity * (1 - normalizedDiscount);
                        TransactionDTO dto = new TransactionDTO();
                        dto.setDate(now);
                        dto.setCustomerName(customerName);
                        dto.setProduct(product.name());
                        dto.setTotalItems(quantity);
                        dto.setTotalCost(lineTotal);
                        dto.setPaymentMethod(paymentMethod);
                        dto.setCity(city);
                        dto.setDiscountApplied(normalizedDiscount > 0 ? 1 : 0);
                        dto.setCustomerCategory(customerCategory);
                        dto.setDiscount(normalizedDiscount);
                        dto.setProductDetails(new ProductDTO(
                                product.id(),
                                product.name(),
                                product.category(),
                                product.price(),
                                product.stock()
                            )
                        );

                        Transaction saved = transactionDAO.save(conn, DTOMapper.toEntity(dto));
                        boolean stockUpdated = productDAO.updateStock(conn, productId, product.stock() - quantity);
                        if (!stockUpdated) {
                            throw new ServiceException("Stock update failed for product ID: " + productId);
                        }

                        TransactionDTO savedDto = DTOMapper.toDTO(saved);
                        createdTransactions.add(savedDto);
                        cartTotal += savedDto.getTotalCost();
                        totalItems += savedDto.getTotalItems();
                    }

                    conn.commit();

                    Map<String, Object> result = new HashMap<>();
                    result.put("transactions", createdTransactions);
                    result.put("totalItems", totalItems);
                    result.put("cartTotal", cartTotal);
                    result.put("lines", createdTransactions.size());
                    return result;
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new ServiceException("Error during atomic cart processing", e);
            }
        });
    }

    private Map<String, Object> buildUserProfile(UserDTO user, String nickname) throws ServiceException {
        List<TransactionDTO> orders = transactionService.findByCustomer(nickname);
        double totalSpent = orders.stream().mapToDouble(TransactionDTO::getTotalCost).sum();

        return Map.of(
                "user", user,
                "orderHistory", orders,
                "totalOrders", orders.size(),
                "totalSpent", totalSpent);
    }

    protected Map<String, Object> getUserProfile(String token, String nickname) throws ServiceException {
        return customerMethod(token, nickname, () -> {
            Optional<UserDTO> userOpt = userService.findByNickname(nickname);
            if (userOpt.isEmpty())
                return null;

            return buildUserProfile(userOpt.get(), nickname);
        });
    }

    /** ADMIN LEVEL OPERATIONS 

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

    public int countTransactionsByDateRange(String token, LocalDateTime start, LocalDateTime end)
            throws ServiceException {
        return adminMethod(token, () -> transactionService.countByDateRange(start, end));
    }

    public List<TransactionDTO> findRecentTransactions(String token, int limit) throws ServiceException {
        return adminMethod(token, () -> transactionService.findRecentTransactions(limit));
    }

    public Map<String, Object> getDashboardStats(String token) throws ServiceException {
        return adminMethod(token, () -> {
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime now = LocalDateTime.now();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", productService.count());
            stats.put("totalUsers", userService.count());
            stats.put("totalTransactions", transactionService.count());
            stats.put("totalPermissions", permissionService.count());
            stats.put("monthlySales", transactionService.calculateTotalSales(startOfMonth, now));
            stats.put("monthlyTransactions", transactionService.countByDateRange(startOfMonth, now));
            stats.put("lowStockProducts", productService.findLowStockProducts(10).size());

            return stats;
        });
    }

    public int getFirstAvailablePermissionId(String token) throws ServiceException {
        return adminMethod(token, () -> {
            return permissionService.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("No permission found in database"))
                    .getId();
        });
    }

    public int countPermissions(String token) throws ServiceException {
        return adminMethod(token, () -> permissionService.count());
    }

    public int countProducts(String token) throws ServiceException {
        return adminMethod(token, () -> productService.count());
    }

    public int countUsers(String token) throws ServiceException {
        return adminMethod(token, () -> userService.count());
    }

    public int countTransactions(String token) throws ServiceException {
        return adminMethod(token, () -> transactionService.count());
    }

    /** UTILITIES 

    public boolean permissionExists(int id) throws ServiceException {
        return permissionService.exists(id);
    }

    public boolean productExists(int id) throws ServiceException {
        return productService.exists(id);
    }

    public boolean userExists(String nickname) throws ServiceException {
        return userService.exists(nickname);
    }

    public boolean transactionExists(long id) throws ServiceException {
        return transactionService.exists(id);
    }

    private String resolveCustomerCategory(String customerName) throws ServiceException {
        Optional<UserDTO> userOpt = userService.findByNickname(customerName);
        if (userOpt.isPresent()) {
            UserDTO user = userOpt.get();
            if (user.getPermission() != null && user.getPermission().getCategory() != null
                    && !user.getPermission().getCategory().isBlank()) {
                return user.getPermission().getCategory();
            }
        }
        return "Customer";
    }

    private Map<String, Object> resolveDiscountForCartItems(
        Map<Integer, Integer> items,
        int sampleWindow) throws ServiceException {

        Map<String, Object> result = new HashMap<>();
        if (items == null || items.isEmpty()) {
            result.put("discount", 0.0f);
            result.put("sampleSize", 0);
            result.put("discountSource", "no_cart_items");
            return result;
        }

        int limit = Math.max(1, sampleWindow);
        double weightedDiscountSum = 0.0;
        int weightedQuantity = 0;
        int sampleSize = 0;

        // Compute average discount from recent transactions for each product and weight by cart quantity.
        for (Map.Entry<Integer, Integer> entry : items.entrySet()) {
            int productId = entry.getKey();
            int quantity = Math.max(0, entry.getValue());
            if (quantity <= 0) {
                continue;
            }

            List<TransactionDTO> productTransactions = resolveRecentTransactionsForProductDiscount(productId, limit);
            if (productTransactions.isEmpty()) {
                continue;
            }

            double productAverage = productTransactions.stream()
                    .mapToDouble(tx -> Math.max(0.0f, Math.min(tx.getDiscount(), 1.0f)))
                    .average()
                    .orElse(0.0);

            weightedDiscountSum += productAverage * quantity;
            weightedQuantity += quantity;
            sampleSize += productTransactions.size();
        }

        float discount = weightedQuantity > 0
                ? (float) Math.max(0.0, Math.min(weightedDiscountSum / weightedQuantity, 1.0))
                : 0.0f;

        result.put("discount", discount);
        result.put("sampleSize", sampleSize);
        result.put("discountSource", weightedQuantity > 0 ? "recent_product_average_discount" : "no_product_history");
        return result;
    }

    private List<TransactionDTO> resolveRecentTransactionsForProductDiscount(int productId, int maxItems)
            throws ServiceException {
        return transactionService.findRecentByProduct(productId, Math.max(1, maxItems));
    }
}

*/
