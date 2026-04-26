package com.cloudstore.server.model.domain;

import java.time.LocalDateTime;

/**
 * Domain record summarizing information about a top customer.
**/
public record TopCustomerSummary(
    String customerName,
    int orderCount,
    double totalSpent,
    LocalDateTime lastOrderDate
) {}
