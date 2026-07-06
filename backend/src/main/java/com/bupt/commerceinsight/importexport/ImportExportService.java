package com.bupt.commerceinsight.importexport;

import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.importexport.vo.ImportErrorVO;
import com.bupt.commerceinsight.importexport.vo.ImportTaskVO;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportExportService {

    private final AtomicLong taskIdGenerator = new AtomicLong(1000);
    private final Map<Long, ImportTaskVO> tasks = new ConcurrentHashMap<>();
    private final List<ImportErrorVO> mockErrors = List.of(
        new ImportErrorVO(18L, "o_totalprice", "-1", "金额不能为负数"),
        new ImportErrorVO(42L, "o_orderdate", "99-13-01", "日期格式错误")
    );

    public ImportTaskVO createImportTask(String tableName, MultipartFile file) {
        long taskId = taskIdGenerator.incrementAndGet();
        String fileName = file == null || file.isEmpty() ? tableName + "_sample.txt" : file.getOriginalFilename();
        ImportTaskVO task = new ImportTaskVO(taskId, tableName, fileName, "running", 0L, 0L, 0L, null, null, null);
        tasks.put(taskId, task);
        return task;
    }

    public ImportTaskVO getTask(Long taskId) {
        return tasks.computeIfAbsent(taskId, id -> new ImportTaskVO(
            id, "orders", "orders_sample.txt", "success", 1000L, 990L, 10L,
            1250L, "2026-07-06 10:00:00", "2026-07-06 10:00:02"
        ));
    }

    public PageResponse<ImportErrorVO> getErrors(Long taskId, int pageNo, int pageSize) {
        int from = Math.min(Math.max(pageNo - 1, 0) * pageSize, mockErrors.size());
        int to = Math.min(from + pageSize, mockErrors.size());
        return new PageResponse<>(pageNo, pageSize, mockErrors.size(), mockErrors.subList(from, to));
    }

    public ByteArrayResource exportTable(String tableName) {
        String csv = switch (tableName) {
            case "orders" -> "orderKey,orderDate,customerName,revenue\n1,1996-01-02,Customer#000000001,172799.49\n";
            case "customer" -> "customerKey,customerName,nationName,accountBalance,marketSegment\n1,Customer#000000001,CHINA,711.56,BUILDING\n";
            default -> "tableName,message\n" + tableName + ",mock export\n";
        };
        return new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
    }
}
