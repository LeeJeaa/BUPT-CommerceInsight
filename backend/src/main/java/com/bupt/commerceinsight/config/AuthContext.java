package com.bupt.commerceinsight.config;

import java.util.Optional;

public final class AuthContext {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        CURRENT_USER.set(user);
    }

    public static Optional<CurrentUser> get() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static CurrentUser requireUser() {
        return get().orElseThrow(() -> new IllegalStateException("No current user in request context"));
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
