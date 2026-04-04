package com.cloudstore.server.service.impl;

import com.cloudstore.server.model.dto.TransactionDTO;
import com.cloudstore.server.model.dto.UserDTO;
import com.cloudstore.server.model.dto.ProductDTO;
import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.*;

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

    /** 
        * Retrieves seller dashboard statistics: total sales, orders count, revenue, average order value, products sold, low stock.
        * Note: Currently returns global statistics as fallback (multi-seller support pending DB schema update).
        * @return A map containing seller dashboard statistics.
        * @throws ServiceException If an error occurs while retrieving statistics.
    **/
    @Override
    public Map<String, Object> getSellerDashboardStats() throws ServiceException {
        System.err.println("=== getSellerDashboardStats called ===");
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime now = LocalDateTime.now();
            
            // Total revenue (all transactions - fallback until seller field is added to transactions table)
            double totalRevenue = 0.0;
            try {
                totalRevenue = transactionService.calculateTotalSales(startOfMonth, now);
                System.err.println("Total revenue: " + totalRevenue);
            } catch (Exception e) {
                System.err.println("Error calculating revenue: " + e.getMessage());
                totalRevenue = 0.0;
            }
            stats.put("totalRevenue", totalRevenue);
            
            // Total orders
            int totalOrders = 0;
            try {
                totalOrders = transactionService.countByDateRange(startOfMonth, now);
                System.err.println("Total orders: " + totalOrders);
            } catch (Exception e) {
                System.err.println("Error counting orders: " + e.getMessage());
                totalOrders = 0;
            }
            stats.put("totalOrders", totalOrders);
            
            // Average order value
            double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;
            stats.put("averageOrderValue", averageOrderValue);
            
            // Products sold (unique products in transactions)
            int productsSold = 0;
            try {
                List<TransactionDTO> allTransactions = transactionService.findRecentTransactions(Integer.MAX_VALUE);
                if (allTransactions != null) {
                    productsSold = (int) allTransactions.stream()
                            .map(TransactionDTO::getProduct)
                            .distinct()
                            .count();
                }
                System.err.println("Products sold: " + productsSold);
            } catch (Exception e) {
                System.err.println("Error counting products sold: " + e.getMessage());
                productsSold = 0;
            }
            stats.put("productsSold", productsSold);
            
            // Total sales volume (same as totalRevenue)
            stats.put("totalSales", totalRevenue);
            
            // Low stock products
            int lowStockProducts = 0;
            try {
                List<ProductDTO> lowStock = productService.findLowStockProducts(10);
                lowStockProducts = lowStock != null ? lowStock.size() : 0;
                System.err.println("Low stock products: " + lowStockProducts);
            } catch (Exception e) {
                System.err.println("Error finding low stock products: " + e.getMessage());
                lowStockProducts = 0;
            }
            stats.put("lowStockProducts", lowStockProducts);
            
            System.err.println("Seller dashboard stats prepared: " + stats);
            return stats;
            
        } catch (Exception e) {
            System.err.println("Fatal error in getSellerDashboardStats: " + e.getMessage());
            e.printStackTrace();
            stats.put("totalRevenue", 0.0);
            stats.put("totalOrders", 0);
            stats.put("averageOrderValue", 0.0);
            stats.put("productsSold", 0);
            stats.put("totalSales", 0.0);
            stats.put("lowStockProducts", 0);
            return stats;
        }
    }

    /** 
        * Retrieves all products available for the seller to manage.
        * Note: Currently returns all products (fallback until multi-seller product ownership is implemented).
        * @return A list of ProductDTOs representing the seller's inventory.
        * @throws ServiceException If an error occurs while retrieving products.
    **/
    @Override
    public List<?> getSellerProducts() throws ServiceException {
        System.err.println("=== getSellerProducts called ===");
        try {
            List<ProductDTO> products = productService.findAll();
            System.err.println("Retrieved " + (products != null ? products.size() : 0) + " products");
            return products;
        } catch (Exception e) {
            System.err.println("Error retrieving seller products: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /** 
        * Retrieves recent sales orders for the seller (all transactions).
        * Note: Currently returns all transactions (fallback until seller_id is added to transactions table).
        * @param limit The maximum number of orders to retrieve.
        * @return A list of maps containing order details (id, customerName, product, totalItems, totalCost, paymentMethod, city).
        * @throws ServiceException If an error occurs while retrieving orders.
    **/
    @Override
    public List<Map<String, Object>> getSellerSalesOrders(int limit) throws ServiceException {
        System.err.println("=== getSellerSalesOrders called with limit=" + limit + " ===");
        try {
            List<TransactionDTO> transactions = transactionService.findRecentTransactions(limit);
            List<Map<String, Object>> orders = new java.util.ArrayList<>();
            
            if (transactions != null) {
                for (TransactionDTO tx : transactions) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("id", tx.getId());
                    order.put("customerName", tx.getCustomerName());
                    order.put("product", tx.getProduct());
                    order.put("totalItems", tx.getTotalItems());
                    order.put("totalCost", tx.getTotalCost());
                    order.put("paymentMethod", tx.getPaymentMethod());
                    order.put("city", tx.getCity());
                    orders.add(order);
                }
            }
            
            System.err.println("Retrieved " + orders.size() + " sales orders");
            return orders;
        } catch (Exception e) {
            System.err.println("Error retrieving seller sales orders: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /** 
        * Retrieves top customers by total spending.
        * Note: Currently returns top customers globally (fallback until seller_id is added to transactions table).
        * @param limit The maximum number of customers to retrieve.
        * @return A list of maps containing customer data (customerName, orderCount, totalSpent, lastOrderDate).
        * @throws ServiceException If an error occurs while retrieving customer data.
    **/
    @Override
    public List<Map<String, Object>> getSellerTopCustomers(int limit) throws ServiceException {
        System.err.println("=== getSellerTopCustomers called with limit=" + limit + " ===");
        try {
            List<TransactionDTO> allTransactions = transactionService.findAll();
            Map<String, Map<String, Object>> customerMap = new java.util.LinkedHashMap<>();
            
            if (allTransactions != null) {
                for (TransactionDTO tx : allTransactions) {
                    String customerName = tx.getCustomerName();
                    customerMap.computeIfAbsent(customerName, k -> {
                        Map<String, Object> customer = new HashMap<>();
                        customer.put("customerName", customerName);
                        customer.put("orderCount", 0);
                        customer.put("totalSpent", 0.0);
                        customer.put("lastOrderDate", tx.getDate());
                        return customer;
                    });
                    
                    Map<String, Object> customer = customerMap.get(customerName);
                    int orderCount = ((Number) customer.get("orderCount")).intValue();
                    double totalSpent = ((Number) customer.get("totalSpent")).doubleValue();
                    
                    customer.put("orderCount", orderCount + 1);
                    customer.put("totalSpent", totalSpent + tx.getTotalCost());
                    customer.put("lastOrderDate", tx.getDate());
                }
            }
            
            // Sort by totalSpent descending and limit
            List<Map<String, Object>> topCustomers = customerMap.values().stream()
                    .sorted((a, b) -> Double.compare(
                            ((Number) b.get("totalSpent")).doubleValue(),
                            ((Number) a.get("totalSpent")).doubleValue()
                    ))
                    .limit(limit)
                    .toList();
            
            System.err.println("Retrieved top " + topCustomers.size() + " customers");
            return topCustomers;
        } catch (Exception e) {
            System.err.println("Error retrieving top customers: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}