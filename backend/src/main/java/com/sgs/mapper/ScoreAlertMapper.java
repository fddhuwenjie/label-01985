package com.sgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.entity.ScoreAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ScoreAlertMapper extends BaseMapper<ScoreAlert> {

    IPage<ScoreAlert> selectAlertPage(Page<ScoreAlert> page,
                                      @Param("studentName") String studentName,
                                      @Param("courseName") String courseName,
                                      @Param("status") Integer status);
}
