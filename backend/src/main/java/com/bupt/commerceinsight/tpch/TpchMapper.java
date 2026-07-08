package com.bupt.commerceinsight.tpch;

import com.bupt.commerceinsight.tpch.vo.Q1RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q5RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q12RecordVO;
import com.bupt.commerceinsight.tpch.vo.Q14RecordVO;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TpchMapper {

    List<Q1RecordVO> q1(@Param("shipDate") LocalDate shipDate);

    List<Q5RecordVO> q5(@Param("regionName") String regionName,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

    List<Q12RecordVO> q12(@Param("shipMode1") String shipMode1,
                          @Param("shipMode2") String shipMode2,
                          @Param("startDate") LocalDate startDate,
                          @Param("endDate") LocalDate endDate);

    List<Q14RecordVO> q14(@Param("month") LocalDate month);
}
