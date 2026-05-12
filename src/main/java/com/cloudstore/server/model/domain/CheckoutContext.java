package com.cloudstore.server.model.domain;

public record CheckoutContext(
    String customerName,
    String customerCategory,
    float discount,
    int discountApplied,
    String discountSource,
    int sampleSize,
    int sampleWindow
) {}
