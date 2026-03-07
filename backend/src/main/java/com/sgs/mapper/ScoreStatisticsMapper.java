package com.sgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgs.entity.ScoreStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreStatisticsMapper extends BaseMapper<ScoreStatistics> {

    /**
     * 按课程统计成绩数据
     */
    @Select("SELECT " +
            "c.id as courseId, " +
            "c.course_name as courseName, " +
            "COUNT(s.id) as studentCount, " +
            "ROUND(AVG(s.score), 2) as avgScore, " +
            "MAX(s.score) as maxScore, " +
            "MIN(s.score) as minScore, " +
            "ROUND(SUM(CASE WHEN s.score >= 60 THEN 1 ELSE 0 END) * 100.0 / COUNT(s.id), 2) as passRate, " +
            "ROUND(SUM(CASE WHEN s.score >= 90 THEN 1 ELSE 0 END) * 100.0 / COUNT(s.id), 2) as excellentRate " +
            "FROM course c " +
            "LEFT JOIN score s ON c.id = s.course_id " +
            "WHERE s.id IS NOT NULL " +
            "GROUP BY c.id, c.course_name")
    List<Map<String, Object>> calculateStatisticsByCourse();

    /**
     * 获取最新统计结果
     */
    @Select("SELECT * FROM score_statistics WHERE stat_type = #{statType} " +
            "AND stat_time = (SELECT MAX(stat_time) FROM score_statistics WHERE stat_type = #{statType})")
    List<ScoreStatistics> getLatestStatistics(String statType);
}
