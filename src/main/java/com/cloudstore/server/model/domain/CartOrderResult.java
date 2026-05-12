package com.cloudstore.server.model.domain;

import com.cloudstore.server.model.entities.Transaction;

import java.util.List;

public record CartOrderResult(
    List<Transaction> transactions,
    int totalItems,
    double cartTotal,
    int lines
) {}
