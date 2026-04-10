package com.cloudstore.server.model.entities;

/**
 * Ruoli utente con gerarchia di privilegi crescente:
 * CUSTOMER (1) → SELLER (2) → ADMIN (3)
 */
public enum Role {
    CUSTOMER(1),
    SELLER(2),
    ADMIN(3);

    private final int id;

    Role(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getRoleName() {
        return this.name().toLowerCase(java.util.Locale.ROOT);
    }

    public boolean hasAccessTo(Role required) {
        if (required == null) return false;
        return this.id >= required.id;
    }

    public static Role fromCategory(String category) {
        if (category == null) return CUSTOMER;
        return switch (category.trim().toLowerCase()) {
            case "admin"  -> ADMIN;
            case "seller" -> SELLER;
            default       -> CUSTOMER;
        };
    }

    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        return CUSTOMER;
    }
}
