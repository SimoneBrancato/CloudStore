package com.cloudstore.server.model.domain;

public record SellerDashboardStats(
    double totalRevenue,
    int totalOrders,
    double averageOrderValue,
    int productsSold,
    double totalSales,
    int lowStockProducts
) {}
