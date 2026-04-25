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

    private final ProductService productService; // Dependency on ProductService to retrieve product information
    private final UserService userService; // Dependency on UserService to retrieve user information
    private final TransactionService transactionService; // Dependency on TransactionService to retrieve transaction information
    private final PermissionService permissionService; // Dependency on PermissionService to retrieve permission information

    // Constructor for DashboardServiceImpl. Initializes all required services.
    public DashboardServiceImpl() throws ServiceException {
        try {
            this.productService = new ProductServiceImpl();
            this.userService = new UserServiceImpl();
            this.transactionService = new TransactionServiceImpl();
            this.permissionService = new PermissionServiceImpl();
        } catch (Exception e) {
            throw new ServiceException("Unable to initialize DashboardServiceImpl", e);
        }
    }

    /** 
     * Retrieves dashboard statistics including total products, users, transactions, permissions, monthly sales, and low stock products.
     * @return A map containing various dashboard statistics.
     * @throws ServiceException If an error occurs while retrieving the statistics.
    */
    @Override
    public Map<String, Object> getDashboardStats() throws ServiceException {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();
        
        stats.put("totalProducts", productService.count());
        stats.put("totalUsers", userService.count());
        stats.put("totalTransactions", transactionService.count());
        stats.put("totalPermissions", permissionService.count());
        stats.put("monthlySales", transactionService.calculateTotalSales(startOfMonth, now));
        stats.put("monthlyTransactions", transactionService.countByDateRange(startOfMonth, now));
        
        List<ProductDTO> lowStock = productService.findLowStockProducts(10);
        stats.put("lowStockProducts", lowStock != null ? lowStock.size() : 0);
        
        return stats;
    }

    /** 
     * Retrieves the user profile information including order history and total spent for a given user nickname.
     * @param nickname The nickname of the user whose profile is to be retrieved.
     * @return A map containing the user's profile information, order history, total orders, and total spent.
     * @throws ServiceException If an error occurs while retrieving the user profile.
    */
    @Override
    public Map<String, Object> getUserProfile(String nickname) throws ServiceException {
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
    }

    /** 
     * Retrieves seller dashboard statistics including total revenue, total orders, average order value, products sold, and low stock products.
     * @return A map containing various seller dashboard statistics.
     * @throws ServiceException If an error occurs while retrieving the statistics.
    */
    @Override
    public Map<String, Object> getSellerDashboardStats() throws ServiceException {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();
        
        double totalRevenue = transactionService.calculateTotalSales(startOfMonth, now);
        stats.put("totalRevenue", totalRevenue);
        
        int totalOrders = transactionService.countByDateRange(startOfMonth, now);
        stats.put("totalOrders", totalOrders);
        
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;
        stats.put("averageOrderValue", averageOrderValue);
        
        int productsSold = transactionService.countDistinctProductsSold();
        stats.put("productsSold", productsSold);
        
        stats.put("totalSales", totalRevenue);
        
        List<ProductDTO> lowStock = productService.findLowStockProducts(10);
        stats.put("lowStockProducts", lowStock != null ? lowStock.size() : 0);
        
        return stats;
    }

    /** 
     * Retrieves a list of products associated with the seller.
     * @return A list of products for the seller.
     * @throws ServiceException If an error occurs while retrieving the products.
    */
    @Override
    public List<?> getSellerProducts() throws ServiceException {
        return productService.findAll();
    }

    /** 
     * Retrieves a list of recent sales orders for the seller, including details such as customer name, product, total items, total cost, payment method, and city.
     * @param limit The maximum number of recent sales orders to retrieve.
     * @return A list of recent sales orders for the seller.
     * @throws ServiceException If an error occurs while retrieving the sales orders.
    */
    @Override
    public List<Map<String, Object>> getSellerSalesOrders(int limit) throws ServiceException {
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
        
        return orders;
    }

    /** 
     * Retrieves a list of top customers for the seller based on total spending.
     * @param limit The maximum number of top customers to retrieve.
     * @return A list of top customers for the seller.
     * @throws ServiceException If an error occurs while retrieving the top customers.
    */
    @Override
    public List<Map<String, Object>> getSellerTopCustomers(int limit) throws ServiceException {
        return transactionService.findTopCustomers(limit);
    }
}