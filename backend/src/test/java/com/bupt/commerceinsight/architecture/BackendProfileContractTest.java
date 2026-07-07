package com.bupt.commerceinsight.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.bupt.commerceinsight.dashboard.JdbcDashboardService;
import com.bupt.commerceinsight.dashboard.MockDashboardService;
import com.bupt.commerceinsight.importexport.JdbcImportExportService;
import com.bupt.commerceinsight.importexport.MockImportExportService;
import com.bupt.commerceinsight.tpcc.JdbcTpccService;
import com.bupt.commerceinsight.tpcc.TpccTransactionLogService;
import com.bupt.commerceinsight.tpcc.dto.NewOrderRequest;
import com.bupt.commerceinsight.tpcc.dto.PaymentRequest;
import com.bupt.commerceinsight.tpch.MockTpchService;
import com.bupt.commerceinsight.tpch.MyBatisTpchService;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

class BackendProfileContractTest {

    @Test
    void mockAndDevImplementationsUseSeparateProfiles() {
        assertThat(profileValues(MockTpchService.class)).containsExactly("mock");
        assertThat(profileValues(MyBatisTpchService.class)).containsExactly("dev", "prod");
        assertThat(profileValues(MockImportExportService.class)).containsExactly("mock");
        assertThat(profileValues(JdbcImportExportService.class)).containsExactly("dev", "prod");
        assertThat(profileValues(MockDashboardService.class)).containsExactly("mock");
        assertThat(profileValues(JdbcDashboardService.class)).containsExactly("dev", "prod");
    }

    @Test
    void tpccTransactionsRollbackForAnyException() throws NoSuchMethodException {
        Transactional newOrder = JdbcTpccService.class
            .getMethod("newOrder", NewOrderRequest.class)
            .getAnnotation(Transactional.class);
        Transactional payment = JdbcTpccService.class
            .getMethod("payment", PaymentRequest.class)
            .getAnnotation(Transactional.class);

        assertThat(newOrder).isNotNull();
        assertThat(payment).isNotNull();
        assertThat(Arrays.asList(newOrder.rollbackFor())).contains(Exception.class);
        assertThat(Arrays.asList(payment.rollbackFor())).contains(Exception.class);
    }

    @Test
    void tpccTransactionLogUsesIndependentTransaction() throws NoSuchMethodException {
        Transactional insert = TpccTransactionLogService.class
            .getMethod("insert", String.class, String.class, String.class, long.class, String.class, Long.class)
            .getAnnotation(Transactional.class);

        assertThat(insert).isNotNull();
        assertThat(insert.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }

    private String[] profileValues(Class<?> type) {
        Profile profile = type.getAnnotation(Profile.class);
        assertThat(profile).isNotNull();
        return profile.value();
    }
}
