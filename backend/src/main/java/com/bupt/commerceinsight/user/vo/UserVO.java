package com.bupt.commerceinsight.user.vo;

public class UserVO {

    private final Long userId;
    private final String username;
    private final String realName;
    private final String email;
    private final String role;
    private final String status;
    private final String createdAt;

    public UserVO(Long userId, String username, String realName, String email, String role, String status, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRealName() {
        return realName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
