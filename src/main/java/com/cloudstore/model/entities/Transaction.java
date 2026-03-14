package com.cloudstore.model.entities;

import java.time.LocalDateTime;

public record Transaction (
    long id,
    LocalDateTime date,
    String CustomerName,
    String Product,
    int TotalItems,
    double TotalCost,
    String PaymentMethod,
    String City,
    int DiscountApplied,
    String CustomerCategory,
    float Discount,
    Product ProductID
) {}