package com.sgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgs.entity.ScoreAlert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ScoreAlertMapper extends BaseMapper<ScoreAlert> {

    @Update("UPDATE score_alert SET status = 1 WHERE id = #{id}")
    int markAsProcessed(@Param("id") Long id);
}
