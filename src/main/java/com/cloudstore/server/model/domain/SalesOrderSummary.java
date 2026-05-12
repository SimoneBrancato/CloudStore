package com.cloudstore.server.model.domain;

public record SalesOrderSummary(
    long id,
    String customerName,
    String product,
    int totalItems,
    double totalCost,
    String paymentMethod,
    String city
) {}
