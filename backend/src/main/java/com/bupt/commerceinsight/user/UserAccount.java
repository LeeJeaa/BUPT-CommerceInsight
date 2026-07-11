package com.bupt.commerceinsight.user;

public record UserAccount(
    Long userId,
    String username,
    String password,
    String realName,
    String email,
    String role,
    String status,
    String createdAt
) {
}
