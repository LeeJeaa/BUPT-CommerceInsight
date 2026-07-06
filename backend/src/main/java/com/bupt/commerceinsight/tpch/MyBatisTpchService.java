package com.bupt.commerceinsight.tpch;

import com.bupt.commerceinsight.config.AuthContext;
import com.bupt.commerceinsight.tpch.vo.Q1RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q5RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q12RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q14RecordVO;
import com.bupt.commerceinsight.tpch.vo.TpchResultVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MyBatisTpchService implements TpchService {

    private final TpchMapper tpchMapper;
    private final JdbcTemplate jdbcTemplate;

    public MyBatisTpchService(TpchMapper tpchMapper, JdbcTemplate jdbcTemplate) {
        this.tpchMapper = tpchMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TpchResultVO<Q1RecordVO> q1(LocalDate shipDate) {
        long start = System.currentTimeMillis();
        List<Q1RecordVO> records = tpchMapper.q1(shipDate);
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(row -> row.getReturnFlag() + "-" + row.getLineStatus()).toList(),
            "series", records.stream().map(Q1RecordVO::getSumCharge).toList()
        );
        return result("TPC-H Q1 定价汇总报表查询", "q1", "shipDate=" + shipDate, start, records, chartData);
    }

    @Override
    public TpchResultVO<Q5RecordVO> q5(String regionName, LocalDate startDate, LocalDate endDate) {
        long start = System.currentTimeMillis();
        List<Q5RecordVO> records = tpchMapper.q5(regionName, startDate, endDate);
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(Q5RecordVO::getNationName).toList(),
            "series", records.stream().map(Q5RecordVO::getRevenue).toList()
        );
        return result(
            "TPC-H Q5 本地供应商收入分析", "q5",
            "regionName=" + regionName + ",startDate=" + startDate + ",endDate=" + endDate,
            start, records, chartData
        );
    }

    @Override
    public TpchResultVO<Q12RecordVO> q12(
        String shipMode1, String shipMode2, LocalDate startDate, LocalDate endDate
    ) {
        long start = System.currentTimeMillis();
        List<Q12RecordVO> records = tpchMapper.q12(shipMode1, shipMode2, startDate, endDate);
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(Q12RecordVO::getShipMode).toList(),
            "highPrioritySeries", records.stream().map(Q12RecordVO::getHighLineCount).toList(),
            "lowPrioritySeries", records.stream().map(Q12RecordVO::getLowLineCount).toList()
        );
        return result(
            "TPC-H Q12 运送方式与订单优先级分析", "q12",
            "shipMode1=" + shipMode1 + ",shipMode2=" + shipMode2
                + ",startDate=" + startDate + ",endDate=" + endDate,
            start, records, chartData
        );
    }

    @Override
    public TpchResultVO<Q14RecordVO> q14(LocalDate month) {
        long start = System.currentTimeMillis();
        List<Q14RecordVO> records = tpchMapper.q14(month);
        BigDecimal promoRevenuePercent = records.isEmpty()
            || records.get(0).getPromoRevenuePercent() == null
            ? BigDecimal.ZERO
            : records.get(0).getPromoRevenuePercent();
        Map<String, Object> chartData = Map.of(
            "gauge", promoRevenuePercent,
            "pieData", List.of(
                Map.of("name", "促销收入", "value", promoRevenuePercent),
                Map.of("name", "非促销收入", "value", new BigDecimal("100").subtract(promoRevenuePercent))
            )
        );
        return result(
            "TPC-H Q14 促销效果查询", "q14", "month=" + month,
            start, records, chartData
        );
    }

    private <T> TpchResultVO<T> result(
        String queryName,
        String queryCode,
        String queryParams,
        long start,
        List<T> records,
        Map<String, Object> chartData
    ) {
        long elapsedMs = Math.max(1L, System.currentTimeMillis() - start);
        Long userId = AuthContext.get().map(user -> user.userId()).orElse(null);
        jdbcTemplate.update("""
            INSERT INTO query_log (query_name, query_params, elapsed_ms, row_count, executed_by)
            VALUES (?, ?, ?, ?, ?)
            """, queryCode, queryParams, elapsedMs, records.size(), userId);
        return new TpchResultVO<>(
            queryName,
            elapsedMs,
            records.size(),
            records,
            chartData,
            "PostgreSQL/MyBatis；正式 EXPLAIN 结果由 D 的性能测试流程归档"
        );
    }
}
