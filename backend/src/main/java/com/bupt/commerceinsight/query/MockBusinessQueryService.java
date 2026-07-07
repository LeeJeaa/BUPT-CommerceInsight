package com.bupt.commerceinsight.query;

import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.query.vo.CustomerQueryVO;
import com.bupt.commerceinsight.query.vo.OrderRevenueVO;
import com.bupt.commerceinsight.query.vo.PartSupplierVO;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("mock")
public class MockBusinessQueryService implements BusinessQueryService {

    private final List<CustomerQueryVO> customers = List.of(
        new CustomerQueryVO(1L, "Customer#000000001", "CHINA", new BigDecimal("711.56"), "BUILDING"),
        new CustomerQueryVO(2L, "Customer#000000002", "JAPAN", new BigDecimal("121.65"), "AUTOMOBILE"),
        new CustomerQueryVO(3L, "Customer#000000003", "CHINA", new BigDecimal("928.12"), "HOUSEHOLD")
    );

    private final List<OrderRevenueVO> orderRevenue = List.of(
        new OrderRevenueVO(1L, "1996-01-02", "Customer#000000001", new BigDecimal("172799.49")),
        new OrderRevenueVO(2L, "1996-02-14", "Customer#000000002", new BigDecimal("84532.10"))
    );

    private final List<PartSupplierVO> partSuppliers = List.of(
        new PartSupplierVO(1001L, "Part-1001", "Supplier#000000001", "CHINA", 8400, new BigDecimal("18.20")),
        new PartSupplierVO(1002L, "Part-1002", "Supplier#000000002", "JAPAN", 6200, new BigDecimal("22.40")),
        new PartSupplierVO(1003L, "Part-1003", "Supplier#000000003", "INDIA", 9100, new BigDecimal("16.70"))
    );

    @Override
    public PageResponse<CustomerQueryVO> customers(String keyword, String nationName, int pageNo, int pageSize) {
        List<CustomerQueryVO> filtered = customers.stream()
            .filter(row -> keyword == null || keyword.isBlank() || row.getCustomerName().contains(keyword))
            .filter(row -> nationName == null || nationName.isBlank() || row.getNationName().equalsIgnoreCase(nationName))
            .toList();
        return page(filtered, pageNo, pageSize);
    }

    @Override
    public PageResponse<OrderRevenueVO> orderRevenue(String startDate, String endDate, int pageNo, int pageSize) {
        return page(orderRevenue, pageNo, pageSize);
    }

    @Override
    public PageResponse<PartSupplierVO> partSupplier(String keyword, int pageNo, int pageSize) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        List<PartSupplierVO> filtered = partSuppliers.stream()
            .filter(row -> normalizedKeyword.isBlank()
                || String.valueOf(row.getPartKey()).equals(normalizedKeyword)
                || row.getPartName().toLowerCase().contains(normalizedKeyword)
                || row.getSupplierName().toLowerCase().contains(normalizedKeyword)
                || row.getNationName().toLowerCase().contains(normalizedKeyword))
            .toList();
        return page(filtered, pageNo, pageSize);
    }

    private <T> PageResponse<T> page(List<T> records, int pageNo, int pageSize) {
        int from = Math.min(Math.max(pageNo - 1, 0) * pageSize, records.size());
        int to = Math.min(from + pageSize, records.size());
        return new PageResponse<>(pageNo, pageSize, records.size(), records.subList(from, to));
    }
}
