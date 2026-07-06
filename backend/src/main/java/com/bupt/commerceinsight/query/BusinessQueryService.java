package com.bupt.commerceinsight.query;

import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.query.vo.CustomerQueryVO;
import com.bupt.commerceinsight.query.vo.OrderRevenueVO;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BusinessQueryService {

    private final List<CustomerQueryVO> customers = List.of(
        new CustomerQueryVO(1L, "Customer#000000001", "CHINA", new BigDecimal("711.56"), "BUILDING"),
        new CustomerQueryVO(2L, "Customer#000000002", "JAPAN", new BigDecimal("121.65"), "AUTOMOBILE"),
        new CustomerQueryVO(3L, "Customer#000000003", "CHINA", new BigDecimal("928.12"), "HOUSEHOLD")
    );

    private final List<OrderRevenueVO> orderRevenue = List.of(
        new OrderRevenueVO(1L, "1996-01-02", "Customer#000000001", new BigDecimal("172799.49")),
        new OrderRevenueVO(2L, "1996-02-14", "Customer#000000002", new BigDecimal("84532.10"))
    );

    public PageResponse<CustomerQueryVO> customers(String keyword, String nationName, int pageNo, int pageSize) {
        List<CustomerQueryVO> filtered = customers.stream()
            .filter(row -> keyword == null || keyword.isBlank() || row.getCustomerName().contains(keyword))
            .filter(row -> nationName == null || nationName.isBlank() || row.getNationName().equalsIgnoreCase(nationName))
            .toList();
        return page(filtered, pageNo, pageSize);
    }

    public PageResponse<OrderRevenueVO> orderRevenue(String startDate, String endDate, int pageNo, int pageSize) {
        return page(orderRevenue, pageNo, pageSize);
    }

    private <T> PageResponse<T> page(List<T> records, int pageNo, int pageSize) {
        int from = Math.min(Math.max(pageNo - 1, 0) * pageSize, records.size());
        int to = Math.min(from + pageSize, records.size());
        return new PageResponse<>(pageNo, pageSize, records.size(), records.subList(from, to));
    }
}
