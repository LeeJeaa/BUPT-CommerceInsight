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

    public Optional<CurrentUser> findByToken(String token) {
        return Optional.ofNullable(usersByToken.get(token));
    }

    public String issue(UserAccount account) {
        String token = UUID.randomUUID().toString();
        usersByToken.put(token, new CurrentUser(
            account.userId(), account.username(), account.role(), account.status()
        ));
        return token;
    }

    public void register(String token, CurrentUser user) {
        usersByToken.put(token, user);
    }
}
