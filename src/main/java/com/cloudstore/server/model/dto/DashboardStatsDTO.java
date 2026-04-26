package com.cloudstore.server.model.dto;

/**
 * Data Transfer Object for representing admin dashboard statistics.
**/
public class DashboardStatsDTO {
    private int totalProducts; // Total number of products in the system
    private int totalUsers; // Total number of registered users
    private int totalTransactions; // Total number of transactions processed
    private int totalPermissions; // Total number of permission categories
    private double monthlySales; // Total sales volume for the current month
    private int monthlyTransactions; // Total number of transactions for the current month
    private int lowStockProducts; // Count of products with stock below threshold

    /**
     * Default constructor for frameworks that require it.
    **/
    public DashboardStatsDTO() {}

    /**
     * Constructor to initialize all fields.
     * @param totalProducts Total number of products in the system.
     * @param totalUsers Total number of registered users.
     * @param totalTransactions Total number of transactions processed.
     * @param totalPermissions Total number of permission categories.
     * @param monthlySales Total sales volume for the current month.
     * @param monthlyTransactions Total number of transactions for the current month.
     * @param lowStockProducts Count of products with stock below threshold.
    **/
    public DashboardStatsDTO(int totalProducts, int totalUsers, int totalTransactions, int totalPermissions, double monthlySales, int monthlyTransactions, int lowStockProducts) {
        this.totalProducts = totalProducts;
        this.totalUsers = totalUsers;
        this.totalTransactions = totalTransactions;
        this.totalPermissions = totalPermissions;
        this.monthlySales = monthlySales;
        this.monthlyTransactions = monthlyTransactions;
        this.lowStockProducts = lowStockProducts;
    }

    /**
     * Gets the total number of products in the system.
     * @return The totalProducts.
    **/
    public int getTotalProducts() { return totalProducts; }

    /**
     * Sets the total number of products in the system.
     * @param totalProducts The totalProducts.
    **/
    public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
    /**
     * Gets the total number of registered users.
     * @return The totalUsers.
    **/
    public int getTotalUsers() { return totalUsers; }

    /**
     * Sets the total number of registered users.
     * @param totalUsers The totalUsers.
    **/
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    /**
     * Gets the total number of transactions processed.
     * @return The totalTransactions.
    **/
    public int getTotalTransactions() { return totalTransactions; }

    /**
     * Sets the total number of transactions processed.
     * @param totalTransactions The totalTransactions.
    **/
    public void setTotalTransactions(int totalTransactions) { this.totalTransactions = totalTransactions; }
    /**
     * Gets the total number of permission categories.
     * @return The totalPermissions.
    **/
    public int getTotalPermissions() { return totalPermissions; }

    /**
     * Sets the total number of permission categories.
     * @param totalPermissions The totalPermissions.
    **/
    public void setTotalPermissions(int totalPermissions) { this.totalPermissions = totalPermissions; }
    /**
     * Gets the total sales volume for the current month.
     * @return The monthlySales.
    **/
    public double getMonthlySales() { return monthlySales; }

    /**
     * Sets the total sales volume for the current month.
     * @param monthlySales The monthlySales.
    **/
    public void setMonthlySales(double monthlySales) { this.monthlySales = monthlySales; }
    /**
     * Gets the total number of transactions for the current month.
     * @return The monthlyTransactions.
    **/
    public int getMonthlyTransactions() { return monthlyTransactions; }

    /**
     * Sets the total number of transactions for the current month.
     * @param monthlyTransactions The monthlyTransactions.
    **/
    public void setMonthlyTransactions(int monthlyTransactions) { this.monthlyTransactions = monthlyTransactions; }
    /**
     * Gets the count of products with stock below threshold.
     * @return The lowStockProducts.
    **/
    public int getLowStockProducts() { return lowStockProducts; }

    /**
     * Sets the count of products with stock below threshold.
     * @param lowStockProducts The lowStockProducts.
    **/
    public void setLowStockProducts(int lowStockProducts) { this.lowStockProducts = lowStockProducts; }
}
