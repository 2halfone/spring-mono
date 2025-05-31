package com.example.model;

/**
 * Role Enum for User Authorization
 * 
 * PHASE 2 - DATABASE INTEGRATION
 * Defines available user roles in the system
 * 
 * Roles:
 * - USER: Basic user privileges
 * - MODERATOR: Intermediate privileges
 * - ADMIN: Full system access
 */
public enum Role {
    USER("USER"),
    MODERATOR("MODERATOR"), 
    ADMIN("ADMIN");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Convert string to Role enum
     * @param value Role string value
     * @return Role enum or null if not found
     */
    public static Role fromString(String value) {
        for (Role role : Role.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }
}
