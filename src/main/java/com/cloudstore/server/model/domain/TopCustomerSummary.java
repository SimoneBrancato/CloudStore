package com.cloudstore.server.model.domain;

import java.time.LocalDateTime;

public record TopCustomerSummary(
    String customerName,
    int orderCount,
    double totalSpent,
    LocalDateTime lastOrderDate
) {}
