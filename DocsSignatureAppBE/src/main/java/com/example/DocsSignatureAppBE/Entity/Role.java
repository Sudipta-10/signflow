package com.example.DocsSignatureAppBE.Entity;

public enum Role {
    ADMIN("ROLE_ADMIN", "Administrator with full system access"),
    USER("ROLE_USER", "Regular user with basic access"),
    MODERATOR("ROLE_MODERATOR", "Moderator with enhanced permissions");

    private final String authority;
    private final String description;

    Role(String authority, String description) {
        this.authority = authority;
        this.description = description;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromAuthority(String authority) {
        for (Role role : Role.values()) {
            if (role.authority.equals(authority)) {
                return role;
            }
        }
        return USER; // default role
    }
}
