package com.bupt.commerceinsight.tpch;

import static org.assertj.core.api.Assertions.assertThat;

import com.bupt.commerceinsight.config.AuthContext;
import com.bupt.commerceinsight.config.CurrentUser;
import com.bupt.commerceinsight.tpch.vo.Q1RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q5RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q12RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q14RecordVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class MyBatisTpchServiceTest {

    @AfterEach
    void clearAuthContext() {
        AuthContext.clear();
    }

    @Test
    void q1MapsAllFrozenFieldsAndWritesQueryLog() {
        Q1RecordVO row = new Q1RecordVO(
            "A", "F", new BigDecimal("10"), new BigDecimal("100"),
            new BigDecimal("95"), new BigDecimal("101"),
            new BigDecimal("5"), new BigDecimal("50"), new BigDecimal("0.05"), 2L
        );
        StubTpchMapper mapper = new StubTpchMapper(List.of(row), List.of());
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate();
        MyBatisTpchService service = new MyBatisTpchService(mapper, jdbcTemplate);
        AuthContext.set(new CurrentUser(1L, "admin", "admin", "approved"));

        var result = service.q1(LocalDate.of(1998, 9, 1));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getSumCharge()).isEqualByComparingTo("101");
        assertThat(result.getRecords().get(0).getAvgPrice()).isEqualByComparingTo("50");
        assertThat(result.getRecords().get(0).getAvgDisc()).isEqualByComparingTo("0.05");
        assertThat(result.getChartData()).containsKey("series");
        assertThat(jdbcTemplate.lastSql).contains("INSERT INTO query_log");
        assertThat(jdbcTemplate.lastArgs).contains("q1", 1L);
    }

    @Test
    void q14NormalizesNullAggregateRowToZeroPercent() {
        StubTpchMapper mapper = new StubTpchMapper(List.of(), Collections.singletonList(null));
        RecordingJdbcTemplate jdbcTemplate = new RecordingJdbcTemplate();
        MyBatisTpchService service = new MyBatisTpchService(mapper, jdbcTemplate);
        AuthContext.set(new CurrentUser(1L, "admin", "admin", "approved"));

        var result = service.q14(LocalDate.of(2020, 9, 1));

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getPromoRevenuePercent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getChartData()).containsEntry("gauge", BigDecimal.ZERO);
        assertThat(jdbcTemplate.lastSql).contains("INSERT INTO query_log");
        assertThat(jdbcTemplate.lastArgs).contains("q14", 1L);
    }

    private static final class RecordingJdbcTemplate extends JdbcTemplate {

        private String lastSql;
        private Object[] lastArgs;

        @Override
        public int update(String sql, Object... args) {
            this.lastSql = sql;
            this.lastArgs = args;
            return 1;
        }
    }

    private record StubTpchMapper(List<Q1RecordVO> q1Rows, List<Q14RecordVO> q14Rows) implements TpchMapper {

        @Override
        public List<Q1RecordVO> q1(LocalDate shipDate) {
            return q1Rows;
        }

        @Override
        public List<Q5RecordVO> q5(String regionName, LocalDate startDate, LocalDate endDate) {
            return List.of();
        }

        @Override
        public List<Q12RecordVO> q12(
            String shipMode1, String shipMode2, LocalDate startDate, LocalDate endDate
        ) {
            return List.of();
        }

        @Override
        public List<Q14RecordVO> q14(LocalDate month) {
            return q14Rows;
        }
    }
}
