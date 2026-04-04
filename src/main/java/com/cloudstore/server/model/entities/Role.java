package com.cloudstore.server.model.entities;

public enum Role {
    CUSTOMER(1),
    SELLER(2),
    ADMIN(3);

    private final int priority;

    Role(int priority) {
        this.priority = priority;
    }

    public boolean hasAccessTo(Role required) {
        return this.priority >= required.priority;
    }

    public static Role fromCategory(String category) {
        if (category == null) return CUSTOMER;
        return switch (category.trim().toLowerCase()) {
            case "admin"  -> ADMIN;
            case "seller" -> SELLER;
            default       -> CUSTOMER;
        };
    }
}
