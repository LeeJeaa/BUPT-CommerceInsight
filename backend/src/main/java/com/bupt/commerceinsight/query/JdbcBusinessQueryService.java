package com.bupt.commerceinsight.query;

import com.bupt.commerceinsight.common.BusinessException;
import com.bupt.commerceinsight.common.ErrorCode;
import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.query.vo.CustomerQueryVO;
import com.bupt.commerceinsight.query.vo.OrderRevenueVO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class JdbcBusinessQueryService implements BusinessQueryService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcBusinessQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResponse<CustomerQueryVO> customers(
        String keyword, String nationName, int pageNo, int pageSize
    ) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND c.c_name ILIKE ?");
            args.add("%" + keyword + "%");
        }
        if (nationName != null && !nationName.isBlank()) {
            where.append(" AND TRIM(n.n_name) = ?");
            args.add(nationName);
        }
        Long total = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM customer c
            JOIN nation n ON n.n_nationkey = c.c_nationkey
            """ + where, Long.class, args.toArray());

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(pageSize);
        pageArgs.add(offset(pageNo, pageSize));
        List<CustomerQueryVO> records = jdbcTemplate.query("""
            SELECT c.c_custkey, c.c_name, TRIM(n.n_name) AS nation_name,
                   c.c_acctbal, TRIM(c.c_mktsegment) AS market_segment
            FROM customer c
            JOIN nation n ON n.n_nationkey = c.c_nationkey
            """ + where + " ORDER BY c.c_custkey LIMIT ? OFFSET ?",
            (resultSet, rowNum) -> new CustomerQueryVO(
                resultSet.getLong("c_custkey"),
                resultSet.getString("c_name"),
                resultSet.getString("nation_name"),
                resultSet.getBigDecimal("c_acctbal"),
                resultSet.getString("market_segment")
            ),
            pageArgs.toArray()
        );
        return new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records);
    }

    @Override
    public PageResponse<OrderRevenueVO> orderRevenue(
        String startDate, String endDate, int pageNo, int pageSize
    ) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (startDate != null && !startDate.isBlank()) {
            where.append(" AND o.o_orderdate >= ?");
            args.add(parseDate(startDate));
        }
        if (endDate != null && !endDate.isBlank()) {
            where.append(" AND o.o_orderdate < ?");
            args.add(parseDate(endDate));
        }
        Long total = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM orders o
            JOIN customer c ON c.c_custkey = o.o_custkey
            """ + where, Long.class, args.toArray());

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(pageSize);
        pageArgs.add(offset(pageNo, pageSize));
        List<OrderRevenueVO> records = jdbcTemplate.query("""
            SELECT o.o_orderkey, o.o_orderdate, c.c_name,
                   COALESCE(SUM(l.l_extendedprice * (1 - l.l_discount)), 0) AS revenue
            FROM orders o
            JOIN customer c ON c.c_custkey = o.o_custkey
            LEFT JOIN lineitem l ON l.l_orderkey = o.o_orderkey
            """ + where + """
             GROUP BY o.o_orderkey, o.o_orderdate, c.c_name
             ORDER BY o.o_orderdate, o.o_orderkey
             LIMIT ? OFFSET ?
            """,
            (resultSet, rowNum) -> new OrderRevenueVO(
                resultSet.getLong("o_orderkey"),
                resultSet.getDate("o_orderdate").toLocalDate().toString(),
                resultSet.getString("c_name"),
                resultSet.getBigDecimal("revenue")
            ),
            pageArgs.toArray()
        );
        return new PageResponse<>(pageNo, pageSize, total == null ? 0 : total, records);
    }

    private int offset(int pageNo, int pageSize) {
        return Math.max(pageNo - 1, 0) * pageSize;
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "日期格式错误，应为 yyyy-MM-dd");
        }
    }
}
