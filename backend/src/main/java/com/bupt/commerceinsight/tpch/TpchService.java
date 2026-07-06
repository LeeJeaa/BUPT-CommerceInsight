package com.bupt.commerceinsight.tpch;

import com.bupt.commerceinsight.tpch.vo.Q1RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q5RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q12RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q14RecordVO;
import com.bupt.commerceinsight.tpch.vo.TpchResultVO;
import java.time.LocalDate;

public interface TpchService {

    TpchResultVO<Q1RecordVO> q1(LocalDate shipDate);

    TpchResultVO<Q5RecordVO> q5(String regionName, LocalDate startDate, LocalDate endDate);

    TpchResultVO<Q12RecordVO> q12(
        String shipMode1, String shipMode2, LocalDate startDate, LocalDate endDate
    );

    TpchResultVO<Q14RecordVO> q14(LocalDate month);
}
