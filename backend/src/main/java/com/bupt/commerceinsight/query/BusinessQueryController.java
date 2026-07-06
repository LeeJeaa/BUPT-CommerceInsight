package com.bupt.commerceinsight.query;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.query.vo.CustomerQueryVO;
import com.bupt.commerceinsight.query.vo.OrderRevenueVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/query")
public class BusinessQueryController {

    private final BusinessQueryService queryService;

    public BusinessQueryController(BusinessQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/customers")
    public ApiResponse<PageResponse<CustomerQueryVO>> customers(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String nationName,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.success(queryService.customers(keyword, nationName, pageNo, pageSize));
    }

    @GetMapping("/order-revenue")
    public ApiResponse<PageResponse<OrderRevenueVO>> orderRevenue(
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(defaultValue = "1") int pageNo,
        @RequestParam(defaultValue = "20") int pageSize
    ) {
        return ApiResponse.success(queryService.orderRevenue(startDate, endDate, pageNo, pageSize));
    }
}
