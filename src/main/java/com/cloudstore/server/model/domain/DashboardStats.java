package com.cloudstore.server.model.domain;

/**
 * Domain record representing aggregated dashboard statistics.
**/
public record DashboardStats(
    int totalProducts,
    int totalUsers,
    int totalTransactions,
    int totalPermissions,
    double monthlySales,
    int monthlyTransactions,
    int lowStockProducts
) {}
