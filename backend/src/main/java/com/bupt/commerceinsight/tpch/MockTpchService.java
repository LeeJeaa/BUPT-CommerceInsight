package com.bupt.commerceinsight.tpch;

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
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockTpchService implements TpchService {

    @Override
    public TpchResultVO<Q1RecordVO> q1(LocalDate shipDate) {
        long start = System.currentTimeMillis();
        List<Q1RecordVO> records = List.of(
            new Q1RecordVO(
                "A", "F", new BigDecimal("37734107.00"), new BigDecimal("56586554400.73"),
                new BigDecimal("53758257134.87"), new BigDecimal("55909065222.83"),
                new BigDecimal("25.52"), new BigDecimal("38273.13"), new BigDecimal("0.05"), 1478493L
            ),
            new Q1RecordVO(
                "N", "O", new BigDecimal("74476040.00"), new BigDecimal("111701729697.74"),
                new BigDecimal("106118230307.61"), new BigDecimal("110367043872.50"),
                new BigDecimal("25.50"), new BigDecimal("38249.12"), new BigDecimal("0.05"), 2920374L
            )
        );
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(row -> row.getReturnFlag() + "-" + row.getLineStatus()).toList(),
            "series", records.stream().map(Q1RecordVO::getSumDiscountedPrice).toList()
        );
        return result("TPC-H Q1 定价汇总报表查询", start, records, chartData, "Mock plan: Aggregate on lineitem by return flag and line status");
    }

    @Override
    public TpchResultVO<Q5RecordVO> q5(String regionName, LocalDate startDate, LocalDate endDate) {
        long start = System.currentTimeMillis();
        List<Q5RecordVO> records = List.of(
            new Q5RecordVO("CHINA", new BigDecimal("12345678.90")),
            new Q5RecordVO("JAPAN", new BigDecimal("9876543.21")),
            new Q5RecordVO("INDIA", new BigDecimal("7654321.11"))
        );
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(Q5RecordVO::getNationName).toList(),
            "series", records.stream().map(Q5RecordVO::getRevenue).toList()
        );
        return result("TPC-H Q5 本地供应商收入分析", start, records, chartData, "Mock plan: Hash Join customer/orders/lineitem/supplier/nation/region");
    }

    @Override
    public TpchResultVO<Q12RecordVO> q12(String shipMode1, String shipMode2, LocalDate startDate, LocalDate endDate) {
        long start = System.currentTimeMillis();
        List<Q12RecordVO> records = List.of(
            new Q12RecordVO(shipMode1, 6202L, 9324L),
            new Q12RecordVO(shipMode2, 5981L, 8876L)
        );
        Map<String, Object> chartData = Map.of(
            "xAxis", records.stream().map(Q12RecordVO::getShipMode).toList(),
            "highPrioritySeries", records.stream().map(Q12RecordVO::getHighLineCount).toList(),
            "lowPrioritySeries", records.stream().map(Q12RecordVO::getLowLineCount).toList()
        );
        return result("TPC-H Q12 运送方式与订单优先级分析", start, records, chartData, "Mock plan: Aggregate orders and lineitem by ship mode");
    }

    @Override
    public TpchResultVO<Q14RecordVO> q14(LocalDate month) {
        long start = System.currentTimeMillis();
        List<Q14RecordVO> records = List.of(new Q14RecordVO(new BigDecimal("16.38")));
        Map<String, Object> chartData = Map.of(
            "gauge", new BigDecimal("16.38"),
            "pieData", List.of(
                Map.of("name", "促销收入", "value", new BigDecimal("16.38")),
                Map.of("name", "非促销收入", "value", new BigDecimal("83.62"))
            )
        );
        return result("TPC-H Q14 促销效果查询", start, records, chartData, "Mock plan: Join lineitem and part, calculate promo revenue");
    }

    private <T> TpchResultVO<T> result(String queryName, long start, List<T> records, Map<String, Object> chartData, String explainPlan) {
        long elapsedMs = Math.max(1L, System.currentTimeMillis() - start);
        return new TpchResultVO<>(queryName, elapsedMs, records.size(), records, chartData, explainPlan);
    }
}
