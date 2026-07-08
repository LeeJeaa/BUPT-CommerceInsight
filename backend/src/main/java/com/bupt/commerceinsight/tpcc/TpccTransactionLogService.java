package com.bupt.commerceinsight.tpcc;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "prod"})
public class TpccTransactionLogService {

    private final JdbcTemplate jdbcTemplate;

    public TpccTransactionLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insert(
        String txId,
        String type,
        String status,
        long elapsedMs,
        String errorMessage,
        Long userId
    ) {
        jdbcTemplate.update("""
            INSERT INTO transaction_log (transaction_id, transaction_type, status, elapsed_ms, error_message, executed_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """, txId, type, status, elapsedMs, truncate(errorMessage), userId);
    }

    private String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }
}
