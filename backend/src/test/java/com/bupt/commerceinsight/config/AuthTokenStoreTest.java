package com.bupt.commerceinsight.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.bupt.commerceinsight.user.UserAccount;
import org.junit.jupiter.api.Test;

class AuthTokenStoreTest {

    @Test
    void defaultStoreContainsNoMockTokensAndIssuesRandomTokens() {
        AuthTokenStore store = new AuthTokenStore();

        assertThat(store.findByToken("mock-token")).isEmpty();
        assertThat(store.findByToken("mock-token-admin")).isEmpty();
        assertThat(store.findByToken("mock-token-user")).isEmpty();

        UserAccount admin = new UserAccount(
            1L, "admin", "password", "Admin", "admin@example.com",
            "admin", "approved", "2026-07-11 10:00:00"
        );
        String first = store.issue(admin);
        String second = store.issue(admin);

        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo("mock-token");
        assertThat(store.findByToken(first)).isPresent();
    }
}
