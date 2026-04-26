package com.cloudstore.server.model.domain;

/**
 * Domain record containing business context for the checkout process.
**/
public record CheckoutContext(
    String customerName,
    String customerCategory,
    float discount,
    int discountApplied,
    String discountSource,
    int sampleSize,
    int sampleWindow
) {}
