package com.bupt.commerceinsight.tpch;

import com.bupt.commerceinsight.common.ApiResponse;
import com.bupt.commerceinsight.tpch.vo.Q1RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q5RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q12RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q14RecordVO;
import com.bupt.commerceinsight.tpch.vo.TpchResultVO;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tpch")
public class TpchController {

    private final TpchService tpchService;

    public TpchController(TpchService tpchService) {
        this.tpchService = tpchService;
    }

    @GetMapping("/q1")
    public ApiResponse<TpchResultVO<Q1RecordVO>> q1(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate shipDate
    ) {
        return ApiResponse.success(tpchService.q1(shipDate));
    }

    @GetMapping("/q5")
    public ApiResponse<TpchResultVO<Q5RecordVO>> q5(
        @RequestParam String regionName,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(tpchService.q5(regionName, startDate, endDate));
    }

    @GetMapping("/q12")
    public ApiResponse<TpchResultVO<Q12RecordVO>> q12(
        @RequestParam String shipMode1,
        @RequestParam String shipMode2,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.success(tpchService.q12(shipMode1, shipMode2, startDate, endDate));
    }

    @GetMapping("/q14")
    public ApiResponse<TpchResultVO<Q14RecordVO>> q14(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month
    ) {
        return ApiResponse.success(tpchService.q14(month));
    }
}
