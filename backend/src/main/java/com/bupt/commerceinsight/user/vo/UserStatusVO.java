package com.bupt.commerceinsight.user.vo;

public class UserStatusVO {

    private final Long userId;
    private final String status;

    public UserStatusVO(Long userId, String status) {
        this.userId = userId;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }
}
