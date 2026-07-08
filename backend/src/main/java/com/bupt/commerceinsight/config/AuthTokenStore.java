package com.bupt.commerceinsight.config;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.bupt.commerceinsight.user.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenStore {

    private final ConcurrentMap<String, CurrentUser> usersByToken = new ConcurrentHashMap<>();

    public AuthTokenStore() {
        usersByToken.put("mock-token", new CurrentUser(1L, "admin", "admin", "approved"));
        usersByToken.put("mock-token-admin", new CurrentUser(1L, "admin", "admin", "approved"));
        usersByToken.put("mock-token-user", new CurrentUser(2L, "user1", "user", "approved"));
    }

    public Optional<CurrentUser> findByToken(String token) {
        return Optional.ofNullable(usersByToken.get(token));
    }

    public String issue(UserAccount account) {
        String token;
        if ("admin".equals(account.username())) {
            token = "mock-token";
        } else if ("user1".equals(account.username())) {
            token = "mock-token-user";
        } else {
            token = UUID.randomUUID().toString();
        }
        usersByToken.put(token, new CurrentUser(
            account.userId(), account.username(), account.role(), account.status()
        ));
        return token;
    }
}
