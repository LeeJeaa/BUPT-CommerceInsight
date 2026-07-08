package com.bupt.commerceinsight.importexport;

import com.bupt.commerceinsight.common.PageResponse;
import com.bupt.commerceinsight.importexport.vo.ImportErrorVO;
import com.bupt.commerceinsight.importexport.vo.ImportTaskVO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

public interface ImportExportService {

    ImportTaskVO createImportTask(String tableName, MultipartFile file);

    ImportTaskVO getTask(Long taskId);

    PageResponse<ImportErrorVO> getErrors(Long taskId, int pageNo, int pageSize);

    ByteArrayResource exportTable(String tableName);
}
