package com.bupt.commerceinsight.dashboard;

import com.bupt.commerceinsight.dashboard.vo.DashboardModuleVO;
import com.bupt.commerceinsight.dashboard.vo.DashboardVO;
import com.bupt.commerceinsight.dashboard.vo.RowCountVO;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockDashboardService implements DashboardService {

    @Override
    public DashboardVO summary() {
        List<RowCountVO> rowCounts = List.of(
            new RowCountVO("region", 5L),
            new RowCountVO("nation", 25L),
            new RowCountVO("supplier", 2000L),
            new RowCountVO("customer", 30000L),
            new RowCountVO("part", 40000L),
            new RowCountVO("partsupp", 160000L),
            new RowCountVO("orders", 300000L),
            new RowCountVO("lineitem", 1199969L)
        );
        return new DashboardVO(
            24,
            "tpc_commerce",
            "SF=0.2 / 课程数据集",
            "PostgreSQL 16 ready",
            rowCounts,
            modules()
        );
    }

    private List<DashboardModuleVO> modules() {
        return List.of(
            new DashboardModuleVO("TPC-H Q1/Q5/Q12/Q14", "ready"),
            new DashboardModuleVO("TPC-C New-Order/Payment", "ready"),
            new DashboardModuleVO("导入清洗与错误日志", "ready"),
            new DashboardModuleVO("并发性能结果展示", "mock-ready")
        );
    }
}
