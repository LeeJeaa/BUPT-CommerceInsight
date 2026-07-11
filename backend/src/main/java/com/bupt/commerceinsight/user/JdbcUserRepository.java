package com.bupt.commerceinsight.user;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"dev", "prod"})
public class JdbcUserRepository implements UserRepository {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void ensureTestAccounts() {
        String adminHash = passwordEncoder.encode("admin123");
        String userHash = passwordEncoder.encode("user123");
        jdbcTemplate.update("""
            INSERT INTO app_user (username, password_hash, real_name, email, role, status)
            VALUES ('admin', ?, 'Admin', 'admin@example.com', 'admin', 'approved')
            ON CONFLICT (username) DO NOTHING
            """, adminHash);
        jdbcTemplate.update("""
            UPDATE app_user
            SET password_hash = ?, updated_at = current_timestamp
            WHERE username = 'admin' AND password_hash = 'change_me_hash'
            """, adminHash);
        jdbcTemplate.update("""
            INSERT INTO app_user (username, password_hash, real_name, email, role, status)
            VALUES ('user1', ?, '普通用户', 'user1@example.com', 'user', 'approved')
            ON CONFLICT (username) DO NOTHING
            """, userHash);
    }

    @Override
    public UserAccount register(String username, String password, String realName, String email) {
        Long userId;
        try {
            userId = jdbcTemplate.queryForObject("""
                INSERT INTO app_user (username, password_hash, real_name, email, role, status)
                VALUES (?, ?, ?, ?, 'user', 'pending')
                RETURNING user_id
                """, Long.class, username, passwordEncoder.encode(password), realName, email);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建用户失败");
        }
        return findById(userId);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        return jdbcTemplate.query("""
            SELECT user_id, username, password_hash, real_name, email, role, status, created_at
            FROM app_user
            WHERE username = ?
            """, this::mapUser, username).stream().findFirst();
    }

    @Override
    public boolean matchesPassword(UserAccount user, String password) {
        return user.password() != null
            && user.password().startsWith("$2")
            && passwordEncoder.matches(password, user.password());
    }

    @Override
    public List<UserAccount> search(String status, String keyword) {
        StringBuilder sql = new StringBuilder("""
            SELECT user_id, username, password_hash, real_name, email, role, status, created_at
            FROM app_user
            WHERE 1 = 1
            """);
        List<Object> args = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = ?");
            args.add(status);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (username ILIKE ? OR real_name ILIKE ? OR email ILIKE ?)");
            String pattern = "%" + keyword + "%";
            args.add(pattern);
            args.add(pattern);
            args.add(pattern);
        }
        sql.append(" ORDER BY user_id");
        return jdbcTemplate.query(sql.toString(), this::mapUser, args.toArray());
    }

    @Override
    public UserAccount updateStatus(Long userId, String status) {
        int updated = jdbcTemplate.update("""
            UPDATE app_user
            SET status = ?, updated_at = current_timestamp
            WHERE user_id = ?
            """, status, userId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return findById(userId);
    }

    private UserAccount findById(Long userId) {
        return jdbcTemplate.query("""
            SELECT user_id, username, password_hash, real_name, email, role, status, created_at
            FROM app_user
            WHERE user_id = ?
            """, this::mapUser, userId).stream().findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "用户不存在"));
    }

    private UserAccount mapUser(java.sql.ResultSet resultSet, int rowNum) throws java.sql.SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new UserAccount(
            resultSet.getLong("user_id"),
            resultSet.getString("username"),
            resultSet.getString("password_hash"),
            resultSet.getString("real_name"),
            resultSet.getString("email"),
            resultSet.getString("role"),
            resultSet.getString("status"),
            createdAt == null ? null : createdAt.toLocalDateTime().format(DATE_TIME_FORMATTER)
        );
    }
}
