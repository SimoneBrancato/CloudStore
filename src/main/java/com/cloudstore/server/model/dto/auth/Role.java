package com.cloudstore.server.model.auth;

public enum Role {
    CUSTOMER(1),
    SELLER(2),
    ADMIN(3);

    private final int id; // Unique identifier for each role

    /**
        * Constructor for the Role enum.
        * @param id The unique identifier for the role.
     */
    Role(int id) {
        this.id = id;
    }

    /**
        * Gets the unique identifier for the role.
        * @return The role's unique identifier.
    **/
    public int getId() {
        return id;
    }

    /**
        * Gets the name of the role in lowercase.
        * @return The role name in lowercase.
    **/
    public String getRoleName() {
        return this.name().toLowerCase(java.util.Locale.ROOT);
    }

    /**
        * Determines if the current role has access to a required role.
        * @param required The required role to check against.
        * @return True if the current role has access, false otherwise.
    **/
    public boolean hasAccessTo(Role required) {
        if (required == null) return false;
        return this.id >= required.id;
    }

    /**
        * Converts a category string to a Role enum value.
        * @param category The category string to convert.
        * @return The corresponding Role enum value, or CUSTOMER if the category is null or unrecognized.
    **/
    public static Role fromCategory(String category) {
        if (category == null) return CUSTOMER;
        return switch (category.trim().toLowerCase()) {
            case "admin"  -> ADMIN;
            case "seller" -> SELLER;
            default       -> CUSTOMER;
        };
    }

    /**
        * Converts an integer ID to a Role enum value.
        * @param id The integer ID to convert.
        * @return The corresponding Role enum value, or CUSTOMER if the ID does not match any role.
    **/
    public static Role fromId(int id) {
        for (Role role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        return CUSTOMER;
    }
}
