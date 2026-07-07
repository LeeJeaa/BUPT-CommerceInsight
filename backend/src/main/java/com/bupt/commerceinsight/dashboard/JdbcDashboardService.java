package com.bupt.commerceinsight.dashboard;

import com.bupt.commerceinsight.dashboard.vo.DashboardModuleVO;
import com.bupt.commerceinsight.dashboard.vo.DashboardVO;
import com.bupt.commerceinsight.dashboard.vo.RowCountVO;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev", "prod"})
public class JdbcDashboardService implements DashboardService {

    private static final List<String> CORE_TABLES = List.of(
        "region", "nation", "supplier", "customer", "part", "partsupp", "orders", "lineitem"
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcDashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public DashboardVO summary() {
        String databaseName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
        Integer tableCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_type = 'BASE TABLE'
            """, Integer.class);
        List<RowCountVO> rowCounts = CORE_TABLES.stream()
            .map(tableName -> new RowCountVO(tableName, countRows(tableName)))
            .toList();
        return new DashboardVO(
            tableCount == null ? 0 : tableCount,
            databaseName,
            dataScale(rowCounts),
            "PostgreSQL connected",
            rowCounts,
            modules()
        );
    }

    private Long countRows(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
    }

    private String dataScale(List<RowCountVO> rowCounts) {
        long totalRows = rowCounts.stream()
            .map(RowCountVO::getRowCount)
            .filter(java.util.Objects::nonNull)
            .mapToLong(Long::longValue)
            .sum();
        return totalRows + " rows";
    }

    private List<DashboardModuleVO> modules() {
        return List.of(
            new DashboardModuleVO("TPC-H Q1/Q5/Q12/Q14", "ready"),
            new DashboardModuleVO("TPC-C New-Order/Payment", "ready"),
            new DashboardModuleVO("导入清洗与错误日志", "ready"),
            new DashboardModuleVO("并发性能结果展示", "ready")
        );
    }
}
