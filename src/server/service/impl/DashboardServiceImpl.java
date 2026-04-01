package service.impl;

import model.dto.TransactionDTO;
import model.dto.UserDTO;
import model.dto.ProductDTO;
import service.exception.ServiceException;
import service.interfaces.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardServiceImpl implements DashboardService {

    // Dependencies on various services to retrieve data for the dashboard
    private final ProductService productService;

    // Dependencies on various services to retrieve data for the dashboard
    private final UserService userService;

    // Dependencies on various services to retrieve data for the dashboard
    private final TransactionService transactionService;

    // Dependencies on various services to retrieve data for the dashboard
    private final PermissionService permissionService;

    /** 
        * Constructor for DashboardServiceImpl.
        * Initializes the required services for retrieving dashboard data.
        * @throws ServiceException If initialization fails due to service instantiation issues.
    **/
    public DashboardServiceImpl() throws ServiceException {
        System.err.println("Initializing DashboardServiceImpl...");
        try {
            this.productService = new ProductServiceImpl();
            this.userService = new UserServiceImpl();
            this.transactionService = new TransactionServiceImpl();
            this.permissionService = new PermissionServiceImpl();
            System.err.println("DashboardServiceImpl initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize DashboardServiceImpl: " + e.getMessage());
            e.printStackTrace();
            throw new ServiceException("Unable to initialize DashboardServiceImpl", e);
        }
    }

    /** 
        * Retrieves various statistics for the dashboard, such as total products, users, transactions, permissions, monthly sales, and low stock products.
        * @return A map containing the dashboard statistics.
        * @throws ServiceException If an error occurs while retrieving any of the statistics.
    **/
    @Override
    public Map<String, Object> getDashboardStats() throws ServiceException {
        System.err.println("=== getDashboardStats called ===");
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime now = LocalDateTime.now();
            
            int totalProducts = 0;
            try {
                totalProducts = productService.count();
                System.err.println("Total products: " + totalProducts);
            } catch (Exception e) {
                System.err.println("Error counting products: " + e.getMessage());
                e.printStackTrace();
                totalProducts = 0;
            }
            stats.put("totalProducts", totalProducts);
            
            int totalUsers = 0;
            try {
                totalUsers = userService.count();
                System.err.println("Total users: " + totalUsers);
            } catch (Exception e) {
                System.err.println("Error counting users: " + e.getMessage());
                e.printStackTrace();
                totalUsers = 0;
            }
            stats.put("totalUsers", totalUsers);
            
            int totalTransactions = 0;
            try {
                totalTransactions = transactionService.count();
                System.err.println("Total transactions: " + totalTransactions);
            } catch (Exception e) {
                System.err.println("Error counting transactions: " + e.getMessage());
                e.printStackTrace();
                totalTransactions = 0;
            }
            stats.put("totalTransactions", totalTransactions);
            
            int totalPermissions = 0;
            try {
                totalPermissions = permissionService.count();
                System.err.println("Total permissions: " + totalPermissions);
            } catch (Exception e) {
                System.err.println("Error counting permissions: " + e.getMessage());
                e.printStackTrace();
                totalPermissions = 0;
            }
            stats.put("totalPermissions", totalPermissions);
            
            double monthlySales = 0.0;
            try {
                monthlySales = transactionService.calculateTotalSales(startOfMonth, now);
                System.err.println("Monthly sales: " + monthlySales);
            } catch (Exception e) {
                System.err.println("Error calculating monthly sales: " + e.getMessage());
                e.printStackTrace();
                monthlySales = 0.0;
            }
            stats.put("monthlySales", monthlySales);
            
            int monthlyTransactions = 0;
            try {
                monthlyTransactions = transactionService.countByDateRange(startOfMonth, now);
                System.err.println("Monthly transactions: " + monthlyTransactions);
            } catch (Exception e) {
                System.err.println("Error counting monthly transactions: " + e.getMessage());
                e.printStackTrace();
                monthlyTransactions = 0;
            }
            stats.put("monthlyTransactions", monthlyTransactions);
            
            int lowStockProducts = 0;
            try {
                List<ProductDTO> lowStock = productService.findLowStockProducts(10);
                lowStockProducts = lowStock != null ? lowStock.size() : 0;
                System.err.println("Low stock products: " + lowStockProducts);
            } catch (Exception e) {
                System.err.println("Error finding low stock products: " + e.getMessage());
                e.printStackTrace();
                lowStockProducts = 0;
            }
            stats.put("lowStockProducts", lowStockProducts);
            
            System.err.println("Dashboard stats prepared: " + stats);
            return stats;
            
        } catch (Exception e) {
            System.err.println("Fatal error in getDashboardStats: " + e.getMessage());
            e.printStackTrace();
            stats.put("totalProducts", 0);
            stats.put("totalUsers", 0);
            stats.put("totalTransactions", 0);
            stats.put("totalPermissions", 0);
            stats.put("monthlySales", 0.0);
            stats.put("monthlyTransactions", 0);
            stats.put("lowStockProducts", 0);
            return stats;
        }
    }

    /** 
        * Retrieves the user profile information for a given nickname, including user details, order history, total orders, and total amount spent.
        * @param nickname The nickname of the user whose profile is being retrieved.
        * @return A map containing the user's profile information, or null if the user is not found.
        * @throws ServiceException If an error occurs while retrieving the user profile information.
    **/
    @Override
    public Map<String, Object> getUserProfile(String nickname) throws ServiceException {
        try {
            Optional<UserDTO> userOpt = userService.findByNickname(nickname);
            if (userOpt.isEmpty()) {
                return null;
            }

            List<TransactionDTO> orders = transactionService.findByCustomer(nickname);
            double totalSpent = orders.stream().mapToDouble(TransactionDTO::getTotalCost).sum();

            Map<String, Object> profile = new HashMap<>();
            profile.put("user", userOpt.get());
            profile.put("orderHistory", orders != null ? orders : List.of());
            profile.put("totalOrders", orders != null ? orders.size() : 0);
            profile.put("totalSpent", totalSpent);

            return profile;
        } catch (Exception e) {
            System.err.println("Error in getUserProfile: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}