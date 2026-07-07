package com.bupt.commerceinsight.tpcc;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.config.AuthContext;
import com.bupt.commerceinsight.tpcc.dto.NewOrderItemRequest;
import com.bupt.commerceinsight.tpcc.dto.NewOrderRequest;
import com.bupt.commerceinsight.tpcc.dto.PaymentRequest;
import com.bupt.commerceinsight.tpcc.vo.NewOrderVO;
import com.bupt.commerceinsight.tpcc.vo.PaymentVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"dev", "prod"})
public class JdbcTpccService implements TpccService {

    private static final DateTimeFormatter ID_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private final JdbcTemplate jdbcTemplate;
    private final TpccTransactionLogService transactionLogService;

    public JdbcTpccService(JdbcTemplate jdbcTemplate, TpccTransactionLogService transactionLogService) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionLogService = transactionLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NewOrderVO newOrder(NewOrderRequest request) {
        long start = System.currentTimeMillis();
        String txId = nextId("NO");
        try {
            setTransactionContext(txId, "new_order");
            requireNewOrderContext(request);

            Long orderId = jdbcTemplate.queryForObject("""
                UPDATE district
                SET d_next_o_id = d_next_o_id + 1
                WHERE d_w_id = ? AND d_id = ?
                RETURNING d_next_o_id - 1
                """, Long.class, request.getWarehouseId(), request.getDistrictId());

            jdbcTemplate.update("""
                INSERT INTO tpcc_orders (o_id, o_d_id, o_w_id, o_c_id, o_entry_d, o_carrier_id, o_ol_cnt, o_all_local)
                VALUES (?, ?, ?, ?, current_timestamp, NULL, ?, 1)
                """, orderId, request.getDistrictId(), request.getWarehouseId(), request.getCustomerId(), request.getItems().size());
            jdbcTemplate.update("""
                INSERT INTO new_order (no_o_id, no_d_id, no_w_id)
                VALUES (?, ?, ?)
                """, orderId, request.getDistrictId(), request.getWarehouseId());

            BigDecimal totalAmount = BigDecimal.ZERO;
            int orderLineNo = 1;
            for (NewOrderItemRequest item : request.getItems()) {
                Map<String, Object> itemStock = requireItemStock(request.getWarehouseId(), item.getItemId());
                BigDecimal itemPrice = (BigDecimal) itemStock.get("item_price");
                BigDecimal amount = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
                updateStock(request.getWarehouseId(), item);
                jdbcTemplate.update("""
                    INSERT INTO order_line (
                        ol_o_id, ol_d_id, ol_w_id, ol_number, ol_i_id, ol_supply_w_id,
                        ol_delivery_d, ol_quantity, ol_amount, ol_dist_info
                    ) VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?, ?)
                    """, orderId, request.getDistrictId(), request.getWarehouseId(), orderLineNo,
                    item.getItemId(), request.getWarehouseId(), item.getQuantity(), amount, distInfo(request.getDistrictId()));
                totalAmount = totalAmount.add(amount);
                orderLineNo++;
            }

            long elapsedMs = elapsed(start);
            insertTransactionLog(txId, "new_order", "committed", elapsedMs, null);
            return new NewOrderVO(txId, "committed", orderId, totalAmount.setScale(2, RoundingMode.HALF_UP), elapsedMs);
        } catch (RuntimeException exception) {
            insertTransactionLog(txId, "new_order", "rolled_back", elapsed(start), exception.getMessage());
            throw exception;
        } catch (Error error) {
            insertTransactionLog(txId, "new_order", "failed", elapsed(start), error.getMessage());
            throw error;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO payment(PaymentRequest request) {
        long start = System.currentTimeMillis();
        String txId = nextId("PAY");
        try {
            setTransactionContext(txId, "payment");
            requirePaymentContext(request);
            jdbcTemplate.update("UPDATE warehouse SET w_ytd = w_ytd + ? WHERE w_id = ?", request.getPaymentAmount(), request.getWarehouseId());
            jdbcTemplate.update("UPDATE district SET d_ytd = d_ytd + ? WHERE d_w_id = ? AND d_id = ?", request.getPaymentAmount(), request.getWarehouseId(), request.getDistrictId());
            BigDecimal newBalance = jdbcTemplate.queryForObject("""
                UPDATE tpcc_customer
                SET c_balance = c_balance - ?,
                    c_ytd_payment = c_ytd_payment + ?,
                    c_payment_cnt = c_payment_cnt + 1
                WHERE c_w_id = ? AND c_d_id = ? AND c_id = ?
                RETURNING c_balance
                """, BigDecimal.class, request.getPaymentAmount(), request.getPaymentAmount(),
                request.getWarehouseId(), request.getDistrictId(), request.getCustomerId());
            jdbcTemplate.update("""
                INSERT INTO history (h_c_id, h_c_d_id, h_c_w_id, h_d_id, h_w_id, h_date, h_amount, h_data)
                VALUES (?, ?, ?, ?, ?, current_timestamp, ?, 'payment')
                """, request.getCustomerId(), request.getDistrictId(), request.getWarehouseId(),
                request.getDistrictId(), request.getWarehouseId(), request.getPaymentAmount());
            long elapsedMs = elapsed(start);
            insertTransactionLog(txId, "payment", "committed", elapsedMs, null);
            return new PaymentVO(txId, "committed", request.getCustomerId(), newBalance, elapsedMs);
        } catch (RuntimeException exception) {
            insertTransactionLog(txId, "payment", "rolled_back", elapsed(start), exception.getMessage());
            throw exception;
        } catch (Error error) {
            insertTransactionLog(txId, "payment", "failed", elapsed(start), error.getMessage());
            throw error;
        }
    }

    private void setTransactionContext(String txId, String changeType) {
        jdbcTemplate.queryForObject("SELECT set_config('app.transaction_id', ?, true)", String.class, txId);
        jdbcTemplate.queryForObject("SELECT set_config('app.change_type', ?, true)", String.class, changeType);
    }

    private void requireNewOrderContext(NewOrderRequest request) {
        queryRequired("""
            SELECT w.w_id
            FROM warehouse w
            JOIN district d ON d.d_w_id = w.w_id
            JOIN tpcc_customer c ON c.c_w_id = d.d_w_id AND c.c_d_id = d.d_id
            WHERE w.w_id = ? AND d.d_id = ? AND c.c_id = ?
            """, request.getWarehouseId(), request.getDistrictId(), request.getCustomerId());
    }

    private void requirePaymentContext(PaymentRequest request) {
        queryRequired("""
            SELECT c.c_id
            FROM warehouse w
            JOIN district d ON d.d_w_id = w.w_id
            JOIN tpcc_customer c ON c.c_w_id = d.d_w_id AND c.c_d_id = d.d_id
            WHERE w.w_id = ? AND d.d_id = ? AND c.c_id = ?
            """, request.getWarehouseId(), request.getDistrictId(), request.getCustomerId());
    }

    private Map<String, Object> requireItemStock(Long warehouseId, Long itemId) {
        try {
            return jdbcTemplate.queryForMap("""
                SELECT i.i_price AS item_price, s.s_quantity AS stock_quantity
                FROM item i JOIN stock s ON s.s_i_id = i.i_id
                WHERE s.s_w_id = ? AND i.i_id = ?
                """, warehouseId, itemId);
        } catch (EmptyResultDataAccessException exception) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品或库存不存在");
        }
    }

    private void updateStock(Long warehouseId, NewOrderItemRequest item) {
        try {
            jdbcTemplate.queryForObject("""
                UPDATE stock
                SET s_quantity = s_quantity - ?,
                    s_ytd = s_ytd + ?,
                    s_order_cnt = s_order_cnt + 1
                WHERE s_w_id = ? AND s_i_id = ? AND s_quantity >= ?
                RETURNING s_quantity
                """, Integer.class, item.getQuantity(), item.getQuantity(), warehouseId, item.getItemId(), item.getQuantity());
        } catch (EmptyResultDataAccessException exception) {
            throw new BusinessException(ErrorCode.CONFLICT, "库存不足，事务已回滚");
        }
    }

    private void queryRequired(String sql, Object... args) {
        try {
            jdbcTemplate.queryForMap(sql, args);
        } catch (EmptyResultDataAccessException exception) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "事务上下文数据不存在");
        }
    }

    private void insertTransactionLog(String txId, String type, String status, long elapsedMs, String errorMessage) {
        Long userId = AuthContext.get().map(user -> user.userId()).orElse(null);
        transactionLogService.insert(txId, type, status, elapsedMs, errorMessage, userId);
    }

    private String distInfo(Long districtId) {
        return "dist-info-%02d".formatted(districtId);
    }

    private String nextId(String prefix) {
        return "%s-%s-%s".formatted(
            prefix,
            LocalDate.now().format(ID_DATE),
            UUID.randomUUID().toString().substring(0, 8)
        );
    }

    private long elapsed(long start) {
        return Math.max(1L, System.currentTimeMillis() - start);
    }
}
