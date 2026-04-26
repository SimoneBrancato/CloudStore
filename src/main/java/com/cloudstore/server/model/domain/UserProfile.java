package com.cloudstore.server.model.domain;

import com.cloudstore.server.model.entities.Transaction;
import com.cloudstore.server.model.entities.User;

import java.util.List;

/**
 * Domain record representing a complete user profile including order history.
**/
public record UserProfile(
    User user,
    List<Transaction> orderHistory,
    int totalOrders,
    double totalSpent
) {}
