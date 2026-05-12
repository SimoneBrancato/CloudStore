package com.cloudstore.server.model.domain;

public record DashboardStats(
    int totalProducts,
    int totalUsers,
    int totalTransactions,
    int totalPermissions,
    double monthlySales,
    int monthlyTransactions,
    int lowStockProducts
) {}
