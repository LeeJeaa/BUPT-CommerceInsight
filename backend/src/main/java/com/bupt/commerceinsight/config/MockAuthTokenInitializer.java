package com.bupt.commerceinsight.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("mock")
public class MockAuthTokenInitializer {

    private final AuthTokenStore tokenStore;

    public MockAuthTokenInitializer(AuthTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @PostConstruct
    void registerMockTokens() {
        CurrentUser admin = new CurrentUser(1L, "admin", "admin", "approved");
        tokenStore.register("mock-token", admin);
        tokenStore.register("mock-token-admin", admin);
        tokenStore.register(
            "mock-token-user",
            new CurrentUser(2L, "user1", "user", "approved")
        );
    }
}
