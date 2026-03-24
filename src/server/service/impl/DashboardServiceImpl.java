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

    private final ProductService productService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final PermissionService permissionService;

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

    @Override
    public Map<String, Object> getDashboardStats() throws ServiceException {
        System.err.println("=== getDashboardStats called ===");
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime now = LocalDateTime.now();
            
            // Conta prodotti
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
            
            // Conta utenti
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
            
            // Conta transazioni
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
            
            // Conta permessi
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
            
            // Calcola vendite mensili
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
            
            // Conta transazioni mensili
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
            
            // Conta prodotti con stock basso
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
            // Restituisci stats vuote invece di lanciare eccezione
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