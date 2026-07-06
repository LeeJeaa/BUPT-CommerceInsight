package com.bupt.commerceinsight.user;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    UserAccount register(String username, String password, String realName, String email);

    Optional<UserAccount> findByUsername(String username);

    boolean matchesPassword(UserAccount user, String password);

    List<UserAccount> search(String status, String keyword);

    UserAccount updateStatus(Long userId, String status);
}
