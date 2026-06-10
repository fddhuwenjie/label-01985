package com.sgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgs.entity.ScoreAlert;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface ScoreAlertMapper extends BaseMapper<ScoreAlert> {

    List<Map<String, Object>> selectAlertList(@Param("status") Integer status,
                                              @Param("alertLevel") String alertLevel,
                                              @Param("studentName") String studentName);
}
