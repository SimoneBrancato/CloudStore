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

    private final ProductService productService;
    private final UserService userService;
    private final TransactionService transactionService;
    private final PermissionService permissionService;

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

    @Override
    public List<?> getSellerProducts() throws ServiceException {
        return productService.findAll();
    }

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

    @Override
    public List<Map<String, Object>> getSellerTopCustomers(int limit) throws ServiceException {
        return transactionService.findTopCustomers(limit);
    }
}