package com.bupt.commerceinsight.dashboard.vo;

import java.util.List;

public class DashboardVO {

    private final Integer tableCount;
    private final String databaseName;
    private final String dataScale;
    private final String dockerStatus;
    private final List<RowCountVO> rowCounts;
    private final List<DashboardModuleVO> modules;

    public DashboardVO(
        Integer tableCount,
        String databaseName,
        String dataScale,
        String dockerStatus,
        List<RowCountVO> rowCounts,
        List<DashboardModuleVO> modules
    ) {
        this.tableCount = tableCount;
        this.databaseName = databaseName;
        this.dataScale = dataScale;
        this.dockerStatus = dockerStatus;
        this.rowCounts = rowCounts;
        this.modules = modules;
    }

    public Integer getTableCount() {
        return tableCount;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDataScale() {
        return dataScale;
    }

    public String getDockerStatus() {
        return dockerStatus;
    }

    public List<RowCountVO> getRowCounts() {
        return rowCounts;
    }

    public List<DashboardModuleVO> getModules() {
        return modules;
    }
}
