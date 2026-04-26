package com.cloudstore.server.model.domain;

import com.cloudstore.server.model.entities.Transaction;

import java.util.List;

/**
 * Domain record representing the result of a processed cart order.
**/
public record CartOrderResult(
    List<Transaction> transactions,
    int totalItems,
    double cartTotal,
    int lines
) {}
