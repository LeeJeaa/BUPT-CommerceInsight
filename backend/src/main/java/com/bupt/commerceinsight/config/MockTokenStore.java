package com.bupt.commerceinsight.config;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MockTokenStore {

    private final Map<String, CurrentUser> usersByToken = Map.of(
        "mock-token", new CurrentUser(1L, "admin", "admin", "approved"),
        "mock-token-admin", new CurrentUser(1L, "admin", "admin", "approved"),
        "mock-token-user", new CurrentUser(2L, "user1", "user", "approved")
    );

    public Optional<CurrentUser> findByToken(String token) {
        return Optional.ofNullable(usersByToken.get(token));
    }
}
