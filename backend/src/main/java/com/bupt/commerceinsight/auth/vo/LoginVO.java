package com.bupt.commerceinsight.auth.vo;

public class LoginVO {

    private final String token;
    private final Long userId;
    private final String username;
    private final String role;
    private final String status;

    public LoginVO(String token, Long userId, String username, String role, String status) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}
