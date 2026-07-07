package com.bupt.commerceinsight.auth.vo;

public class RegisterVO {

    private final Long userId;
    private final String username;
    private final String status;

    public RegisterVO(Long userId, String username, String status) {
        this.userId = userId;
        this.username = username;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }
}
