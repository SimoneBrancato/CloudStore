package com.cloudstore.server.model.domain;

/**
 * Domain record representing dashboard statistics specific to a seller.
**/
public record SellerDashboardStats(
    double totalRevenue,
    int totalOrders,
    double averageOrderValue,
    int productsSold,
    double totalSales,
    int lowStockProducts
) {}
