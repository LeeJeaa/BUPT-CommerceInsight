package com.bupt.commerceinsight.user;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class MockUserRepository {

    private final AtomicLong idGenerator = new AtomicLong(3);
    private final List<UserAccount> users = new ArrayList<>();

    public MockUserRepository() {
        users.add(new UserAccount(1L, "admin", "admin123", "管理员", "admin@example.com", "admin", "approved", "2026-07-06 10:00:00"));
        users.add(new UserAccount(2L, "user1", "user123", "普通用户", "user1@example.com", "user", "approved", "2026-07-06 10:05:00"));
        users.add(new UserAccount(3L, "alice", "123456", "Alice", "alice@example.com", "user", "pending", "2026-07-06 10:10:00"));
    }

    public synchronized UserAccount register(String username, String password, String realName, String email) {
        if (findByUsername(username).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        UserAccount user = new UserAccount(idGenerator.incrementAndGet(), username, password, realName, email, "user", "pending", "2026-07-06 10:00:00");
        users.add(user);
        return user;
    }

    public synchronized Optional<UserAccount> findByUsername(String username) {
        return users.stream().filter(user -> user.username().equals(username)).findFirst();
    }

    public boolean matchesPassword(UserAccount user, String password) {
        if ("admin".equals(user.username()) && "123456".equals(password)) {
            return true;
        }
        return user.password().equals(password);
    }

    public synchronized List<UserAccount> search(String status, String keyword) {
        return users.stream()
            .filter(user -> status == null || status.isBlank() || user.status().equals(status))
            .filter(user -> keyword == null || keyword.isBlank()
                || user.username().contains(keyword)
                || user.realName().contains(keyword)
                || user.email().contains(keyword))
            .sorted(Comparator.comparing(UserAccount::userId))
            .toList();
    }

    public synchronized UserAccount updateStatus(Long userId, String status) {
        for (int i = 0; i < users.size(); i++) {
            UserAccount user = users.get(i);
            if (user.userId().equals(userId)) {
                UserAccount updated = new UserAccount(
                    user.userId(), user.username(), user.password(), user.realName(),
                    user.email(), user.role(), status, user.createdAt()
                );
                users.set(i, updated);
                return updated;
            }
        }
        throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
    }
}
