package com.bupt.commerceinsight.config;

public record CurrentUser(Long userId, String username, String role, String status) {

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }
}
