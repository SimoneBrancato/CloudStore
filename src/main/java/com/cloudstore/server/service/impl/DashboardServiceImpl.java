package com.cloudstore.server.service.impl;

import com.cloudstore.server.service.exception.ServiceException;
import com.cloudstore.server.service.interfaces.*;
import com.cloudstore.server.model.entities.*;
import com.cloudstore.server.model.domain.*;

import java.time.LocalDateTime;
import java.util.List;
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
         * @return A DashboardStats entity containing various dashboard statistics.
         * @throws ServiceException If an error occurs while retrieving the statistics.
    **/
    @Override
    public DashboardStats getDashboardStats() throws ServiceException {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();
        
        List<Product> lowStock = productService.findLowStockProducts(10);

        return new DashboardStats(
            productService.count(),
            userService.count(),
            transactionService.count(),
            permissionService.count(),
            transactionService.calculateTotalSales(startOfMonth, now),
            transactionService.countByDateRange(startOfMonth, now),
            lowStock != null ? lowStock.size() : 0
        );
    }

    /** 
         * Retrieves the user profile information including order history and total spent for a given user nickname.
         * @param nickname The nickname of the user whose profile is to be retrieved.
         * @return A UserProfile entity containing the user's profile information, or null if user not found.
         * @throws ServiceException If an error occurs while retrieving the user profile.
    **/
    @Override
    public UserProfile getUserProfile(String nickname) throws ServiceException {
        Optional<User> userOpt = userService.findByNickname(nickname);
        if (userOpt.isEmpty()) {
            return null;
        }

        List<Transaction> orders = transactionService.findByCustomer(nickname);
        double totalSpent = orders.stream().mapToDouble(Transaction::TotalCost).sum();

        return new UserProfile(
            userOpt.get(),
            orders != null ? orders : List.of(),
            orders != null ? orders.size() : 0,
            totalSpent
        );
    }

    /** 
         * Retrieves seller dashboard statistics including total revenue, total orders, average order value, products sold, and low stock products.
         * @return A SellerDashboardStats entity containing various seller dashboard statistics.
         * @throws ServiceException If an error occurs while retrieving the statistics.
    **/
    @Override
    public SellerDashboardStats getSellerDashboardStats() throws ServiceException {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();
        
        double totalRevenue = transactionService.calculateTotalSales(startOfMonth, now);
        int totalOrders = transactionService.countByDateRange(startOfMonth, now);
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0.0;
        int productsSold = transactionService.countDistinctProductsSold();
        List<Product> lowStock = productService.findLowStockProducts(10);

        return new SellerDashboardStats(
            totalRevenue,
            totalOrders,
            averageOrderValue,
            productsSold,
            totalRevenue,
            lowStock != null ? lowStock.size() : 0
        );
    }

    /** 
         * Retrieves a list of products associated with the seller.
         * @return A list of Product entities for the seller.
         * @throws ServiceException If an error occurs while retrieving the products.
    **/
    @Override
    public List<Product> getSellerProducts() throws ServiceException {
        return productService.findAll();
    }

    /** 
         * Retrieves a list of recent sales orders for the seller.
         * @param limit The maximum number of recent sales orders to retrieve.
         * @return A list of SalesOrderSummary entities for the seller.
         * @throws ServiceException If an error occurs while retrieving the sales orders.
    **/
    @Override
    public List<SalesOrderSummary> getSellerSalesOrders(int limit) throws ServiceException {
        List<Transaction> transactions = transactionService.findRecentTransactions(limit);
        
        if (transactions == null) {
            return List.of();
        }

        return transactions.stream()
                .map(tx -> new SalesOrderSummary(
                    tx.id(), tx.CustomerName(), tx.Product(),
                    tx.TotalItems(), tx.TotalCost(), tx.PaymentMethod(), tx.City()
                ))
                .toList();
    }

    /** 
         * Retrieves a list of top customers for the seller based on total spending.
         * @param limit The maximum number of top customers to retrieve.
         * @return A list of TopCustomerSummary entities for the seller.
         * @throws ServiceException If an error occurs while retrieving the top customers.
    **/
    @Override
    public List<TopCustomerSummary> getSellerTopCustomers(int limit) throws ServiceException {
        return transactionService.findTopCustomers(limit);
    }
}