package com.cloudstore.server.model.domain;

/**
 * Domain record providing a summary of a sales order.
**/
public record SalesOrderSummary(
    long id,
    String customerName,
    String product,
    int totalItems,
    double totalCost,
    String paymentMethod,
    String city
) {}
