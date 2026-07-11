package com.bupt.commerceinsight.dashboard.vo;

public class DashboardModuleVO {

    private final String name;
    private final String status;

    public DashboardModuleVO(String name, String status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}
