package com.bupt.commerceinsight.query;

import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.query.vo.CustomerQueryVO;
import com.bupt.commerceinsight.query.vo.OrderRevenueVO;

public interface BusinessQueryService {

    PageResponse<CustomerQueryVO> customers(String keyword, String nationName, int pageNo, int pageSize);

    PageResponse<OrderRevenueVO> orderRevenue(String startDate, String endDate, int pageNo, int pageSize);
}
