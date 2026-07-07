package com.bupt.commerceinsight.importexport;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.config.AuthContext;
import com.bupt.commerceinsight.importexport.vo.ImportErrorVO;
import com.bupt.commerceinsight.importexport.vo.ImportTaskVO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("dev")
public class JdbcImportExportService implements ImportExportService {

    private static final Set<String> IMPORT_TABLES = Set.of("orders", "lineitem");
    private static final Set<String> EXPORT_TABLES = Set.of(
        "region", "nation", "supplier", "part", "partsupp", "customer", "orders", "lineitem",
        "warehouse", "district", "tpcc_customer", "history", "item", "stock",
        "tpcc_orders", "new_order", "order_line", "import_task",
        "import_error_log", "query_log", "transaction_log", "performance_result", "stock_change_log"
    );
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JdbcTemplate jdbcTemplate;

    public JdbcImportExportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ImportTaskVO createImportTask(String tableName, MultipartFile file) {
        if (!IMPORT_TABLES.contains(tableName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统演示导入仅支持 orders 和 lineitem");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择导入文件");
        }
        Long taskId = insertTask(tableName, file.getOriginalFilename());
        ImportTaskVO running = getTask(taskId);
        try {
            ImportSummary summary = processFile(taskId, tableName, file.getBytes());
            jdbcTemplate.update("""
                UPDATE import_task
                SET status = 'success', total_rows = ?, success_rows = ?, failed_rows = ?,
                    ended_at = current_timestamp
                WHERE task_id = ?
                """, summary.totalRows(), summary.successRows(), summary.failedRows(), taskId);
        } catch (IOException exception) {
            markFailed(taskId);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "读取导入文件失败");
        } catch (RuntimeException exception) {
            markFailed(taskId);
            throw exception;
        }
        return running;
    }

    @Override
    public ImportTaskVO getTask(Long taskId) {
        return jdbcTemplate.query("""
            SELECT task_id, table_name, file_name, status, total_rows, success_rows, failed_rows,
                   CASE
                       WHEN started_at IS NULL OR ended_at IS NULL THEN NULL
                       ELSE ROUND(EXTRACT(EPOCH FROM (ended_at - started_at)) * 1000)::bigint
                   END AS elapsed_ms,
                   started_at, ended_at
            FROM import_task
            WHERE task_id = ?
            """, (resultSet, rowNum) -> new ImportTaskVO(
                resultSet.getLong("task_id"),
                resultSet.getString("table_name"),
                resultSet.getString("file_name"),
                resultSet.getString("status"),
                resultSet.getLong("total_rows"),
                resultSet.getLong("success_rows"),
                resultSet.getLong("failed_rows"),
                nullableLong(resultSet, "elapsed_ms"),
                formatTimestamp(resultSet.getTimestamp("started_at")),
                formatTimestamp(resultSet.getTimestamp("ended_at"))
            ), taskId).stream().findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "导入任务不存在"));
    }

    @Override
    public PageResponse<ImportErrorVO> getErrors(Long taskId, int pageNo, int pageSize) {
        Long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM import_error_log WHERE task_id = ?", Long.class, taskId
        );
        List<ImportErrorVO> records = jdbcTemplate.query("""
            SELECT line_number, field_name, field_value, error_reason
            FROM import_error_log
            WHERE task_id = ?
            ORDER BY line_number, error_id
            LIMIT ? OFFSET ?
            """, (resultSet, rowNum) -> new ImportErrorVO(
                resultSet.getLong("line_number"),
                resultSet.getString("field_name"),
                resultSet.getString("field_value"),
                resultSet.getString("error_reason")
            ), taskId, pageSize, offset(pageNo, pageSize));
        return new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records);
    }

    @Override
    public ByteArrayResource exportTable(String tableName) {
        if (!EXPORT_TABLES.contains(tableName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持导出该表");
        }
        String csv = jdbcTemplate.query("SELECT * FROM " + tableName, resultSet -> {
            StringBuilder builder = new StringBuilder();
            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            for (int index = 1; index <= columnCount; index++) {
                if (index > 1) {
                    builder.append(',');
                }
                builder.append(csvEscape(metadata.getColumnLabel(index)));
            }
            builder.append('\n');
            while (resultSet.next()) {
                for (int index = 1; index <= columnCount; index++) {
                    if (index > 1) {
                        builder.append(',');
                    }
                    builder.append(csvEscape(resultSet.getObject(index)));
                }
                builder.append('\n');
            }
            return builder.toString();
        });
        return new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
    }

    private Long insertTask(String tableName, String fileName) {
        Long taskId = jdbcTemplate.queryForObject("""
            INSERT INTO import_task (
                table_name, file_name, status, total_rows, success_rows, failed_rows,
                started_at, created_by
            ) VALUES (?, ?, 'running', 0, 0, 0, current_timestamp, ?)
            RETURNING task_id
            """, Long.class, tableName, fileName == null ? tableName + ".tbl" : fileName,
            AuthContext.requireUser().userId());
        if (taskId == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "创建导入任务失败");
        }
        return taskId;
    }

    private ImportSummary processFile(Long taskId, String tableName, byte[] bytes) {
        List<ImportErrorRow> errors = new ArrayList<>();
        int totalRows = 0;
        int successRows;
        try (BufferedReader reader = new BufferedReader(
            new StringReader(new String(bytes, StandardCharsets.UTF_8))
        )) {
            if ("orders".equals(tableName)) {
                List<OrderRow> validRows = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    totalRows++;
                    parseOrder(line, totalRows, validRows, errors);
                }
                successRows = insertOrders(validRows);
            } else {
                List<LineItemRow> validRows = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    totalRows++;
                    parseLineItem(line, totalRows, validRows, errors);
                }
                successRows = insertLineItems(validRows);
            }
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "读取导入文件失败");
        }
        insertErrors(taskId, errors);
        return new ImportSummary(totalRows, successRows, totalRows - successRows);
    }

    private void parseOrder(
        String line, long lineNumber, List<OrderRow> validRows, List<ImportErrorRow> errors
    ) {
        String[] fields = split(line);
        if (fields.length < 9) {
            errors.add(new ImportErrorRow(lineNumber, "row", line, "字段数量不足"));
            return;
        }
        try {
            long orderKey = Long.parseLong(fields[0]);
            long customerKey = Long.parseLong(fields[1]);
            String orderStatus = fields[2];
            BigDecimal totalPrice = new BigDecimal(fields[3]);
            LocalDate orderDate = LocalDate.parse(fields[4]);
            if (!Set.of("O", "F", "P").contains(orderStatus)) {
                errors.add(new ImportErrorRow(lineNumber, "o_orderstatus", orderStatus, "订单状态非法"));
                return;
            }
            if (totalPrice.signum() < 0) {
                errors.add(new ImportErrorRow(lineNumber, "o_totalprice", fields[3], "金额不能为负数"));
                return;
            }
            validRows.add(new OrderRow(
                orderKey, customerKey, orderStatus, totalPrice, orderDate,
                fields[5], fields[6], Integer.parseInt(fields[7]), fields[8]
            ));
        } catch (NumberFormatException exception) {
            errors.add(new ImportErrorRow(lineNumber, "numeric", line, "数值格式错误"));
        } catch (DateTimeParseException exception) {
            errors.add(new ImportErrorRow(lineNumber, "o_orderdate", fields[4], "日期格式错误"));
        }
    }

    private void parseLineItem(
        String line, long lineNumber, List<LineItemRow> validRows, List<ImportErrorRow> errors
    ) {
        String[] fields = split(line);
        if (fields.length < 16) {
            errors.add(new ImportErrorRow(lineNumber, "row", line, "字段数量不足"));
            return;
        }
        try {
            BigDecimal quantity = new BigDecimal(fields[4]);
            BigDecimal discount = new BigDecimal(fields[6]);
            LocalDate shipDate = LocalDate.parse(fields[10]);
            if (quantity.signum() <= 0) {
                errors.add(new ImportErrorRow(lineNumber, "l_quantity", fields[4], "数量必须大于0"));
                return;
            }
            if (discount.signum() < 0 || discount.compareTo(BigDecimal.ONE) > 0) {
                errors.add(new ImportErrorRow(lineNumber, "l_discount", fields[6], "折扣范围错误"));
                return;
            }
            validRows.add(new LineItemRow(
                Long.parseLong(fields[0]), Long.parseLong(fields[1]), Long.parseLong(fields[2]),
                Integer.parseInt(fields[3]), quantity, new BigDecimal(fields[5]), discount,
                new BigDecimal(fields[7]), fields[8], fields[9], shipDate,
                LocalDate.parse(fields[11]), LocalDate.parse(fields[12]), fields[13], fields[14], fields[15]
            ));
        } catch (NumberFormatException exception) {
            errors.add(new ImportErrorRow(lineNumber, "numeric", line, "数值格式错误"));
        } catch (DateTimeParseException exception) {
            errors.add(new ImportErrorRow(lineNumber, "l_shipdate", fields[10], "发货日期格式错误"));
        }
    }

    private int insertOrders(List<OrderRow> rows) {
        int[][] counts = jdbcTemplate.batchUpdate("""
            INSERT INTO orders (
                o_orderkey, o_custkey, o_orderstatus, o_totalprice, o_orderdate,
                o_orderpriority, o_clerk, o_shippriority, o_comment
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (o_orderkey) DO NOTHING
            """, rows, 500, (statement, row) -> {
                statement.setLong(1, row.orderKey());
                statement.setLong(2, row.customerKey());
                statement.setString(3, row.orderStatus());
                statement.setBigDecimal(4, row.totalPrice());
                statement.setDate(5, Date.valueOf(row.orderDate()));
                statement.setString(6, row.orderPriority());
                statement.setString(7, row.clerk());
                statement.setInt(8, row.shipPriority());
                statement.setString(9, row.comment());
            });
        return successfulCount(counts);
    }

    private int insertLineItems(List<LineItemRow> rows) {
        int[][] counts = jdbcTemplate.batchUpdate("""
            INSERT INTO lineitem (
                l_orderkey, l_partkey, l_suppkey, l_linenumber, l_quantity,
                l_extendedprice, l_discount, l_tax, l_returnflag, l_linestatus,
                l_shipdate, l_commitdate, l_receiptdate, l_shipinstruct, l_shipmode, l_comment
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (l_orderkey, l_linenumber) DO NOTHING
            """, rows, 500, (statement, row) -> {
                statement.setLong(1, row.orderKey());
                statement.setLong(2, row.partKey());
                statement.setLong(3, row.supplierKey());
                statement.setInt(4, row.lineNumber());
                statement.setBigDecimal(5, row.quantity());
                statement.setBigDecimal(6, row.extendedPrice());
                statement.setBigDecimal(7, row.discount());
                statement.setBigDecimal(8, row.tax());
                statement.setString(9, row.returnFlag());
                statement.setString(10, row.lineStatus());
                statement.setDate(11, Date.valueOf(row.shipDate()));
                statement.setDate(12, Date.valueOf(row.commitDate()));
                statement.setDate(13, Date.valueOf(row.receiptDate()));
                statement.setString(14, row.shipInstruction());
                statement.setString(15, row.shipMode());
                statement.setString(16, row.comment());
            });
        return successfulCount(counts);
    }

    private void insertErrors(Long taskId, List<ImportErrorRow> errors) {
        jdbcTemplate.batchUpdate("""
            INSERT INTO import_error_log (task_id, line_number, field_name, field_value, error_reason)
            VALUES (?, ?, ?, ?, ?)
            """, errors, 500, (statement, error) -> {
                statement.setLong(1, taskId);
                statement.setLong(2, error.lineNumber());
                statement.setString(3, error.fieldName());
                statement.setString(4, error.fieldValue());
                statement.setString(5, error.errorReason());
            });
    }

    private void markFailed(Long taskId) {
        jdbcTemplate.update("""
            UPDATE import_task
            SET status = 'failed', ended_at = current_timestamp
            WHERE task_id = ?
            """, taskId);
    }

    private int successfulCount(int[][] batches) {
        int success = 0;
        for (int[] counts : batches) {
            for (int count : counts) {
                if (count != 0 && count != Statement.EXECUTE_FAILED) {
                    success++;
                }
            }
        }
        return success;
    }

    private String[] split(String line) {
        String delimiter = line.contains("|") ? "\\|" : ",";
        String[] fields = line.split(delimiter, -1);
        if (line.endsWith("|") && fields.length > 0) {
            return java.util.Arrays.copyOf(fields, fields.length - 1);
        }
        return fields;
    }

    private Long nullableLong(java.sql.ResultSet resultSet, String column) throws java.sql.SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private String formatTimestamp(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER);
    }

    private int offset(int pageNo, int pageSize) {
        return Math.max(pageNo - 1, 0) * pageSize;
    }

    private String csvEscape(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private record ImportSummary(int totalRows, int successRows, int failedRows) {
    }

    private record ImportErrorRow(long lineNumber, String fieldName, String fieldValue, String errorReason) {
    }

    private record OrderRow(
        long orderKey,
        long customerKey,
        String orderStatus,
        BigDecimal totalPrice,
        LocalDate orderDate,
        String orderPriority,
        String clerk,
        int shipPriority,
        String comment
    ) {
    }

    private record LineItemRow(
        long orderKey,
        long partKey,
        long supplierKey,
        int lineNumber,
        BigDecimal quantity,
        BigDecimal extendedPrice,
        BigDecimal discount,
        BigDecimal tax,
        String returnFlag,
        String lineStatus,
        LocalDate shipDate,
        LocalDate commitDate,
        LocalDate receiptDate,
        String shipInstruction,
        String shipMode,
        String comment
    ) {
    }
}
