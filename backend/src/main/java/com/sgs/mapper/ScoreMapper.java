package com.sgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgs.entity.Score;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ScoreMapper extends BaseMapper<Score> {

    @Select("SELECT c.course_name AS courseName, AVG(s.score) AS avgScore, " +
            "MAX(s.score) AS maxScore, MIN(s.score) AS minScore, COUNT(*) AS studentCount " +
            "FROM score s INNER JOIN course c ON s.course_id = c.id " +
            "WHERE s.semester = #{semester} GROUP BY c.id, c.course_name")
    List<Map<String, Object>> selectScoreStatsBySemester(@Param("semester") String semester);

    @Select("SELECT st.name AS studentName, st.student_no AS studentNo, " +
            "SUM(s.score) AS totalScore, AVG(s.score) AS avgScore " +
            "FROM score s INNER JOIN student st ON s.student_id = st.id " +
            "WHERE s.semester = #{semester} GROUP BY st.id, st.name, st.student_no " +
            "ORDER BY totalScore DESC")
    List<Map<String, Object>> selectStudentRanking(@Param("semester") String semester);

    /**
     * 学生历次考试成绩趋势（按时间排序，可按 courseId 筛选科目）。
     * 横轴：考试时间（create_time / semester），纵轴：分数。
     */
    @Select("<script>" +
            "SELECT s.id AS scoreId, s.score AS score, s.semester AS semester, " +
            "s.create_time AS examTime, c.id AS courseId, c.course_name AS courseName " +
            "FROM score s INNER JOIN course c ON s.course_id = c.id " +
            "WHERE s.student_id = #{studentId} " +
            "<if test='courseId != null'> AND s.course_id = #{courseId} </if>" +
            "ORDER BY s.create_time ASC, s.id ASC" +
            "</script>")
    List<Map<String, Object>> selectScoreTrend(@Param("studentId") Long studentId,
                                               @Param("courseId") Long courseId);

    /**
     * 检测同一学生、同一科目（按 course_name 匹配，跨学期同一门课）连续两次考试下降超过阈值的成绩对。
     * 使用窗口函数 LAG 在 SQL 层完成计算，禁止在 Java 层循环。
     * 返回字段与 score_alert 表对应。
     */
    @Select("SELECT t.student_id AS studentId, t.student_name AS studentName, " +
            "       t.course_id AS courseId, t.course_name AS courseName, " +
            "       t.previous_score AS previousScore, t.current_score AS currentScore, " +
            "       (t.previous_score - t.current_score) AS dropValue, " +
            "       t.previous_id AS previousScoreId, t.current_id AS currentScoreId, " +
            "       CASE " +
            "           WHEN (t.previous_score - t.current_score) >= 30 THEN 'HIGH' " +
            "           WHEN (t.previous_score - t.current_score) >= 20 THEN 'MEDIUM' " +
            "           ELSE 'LOW' " +
            "       END AS alertLevel " +
            "FROM ( " +
            "    SELECT s.id AS current_id, s.student_id, st.name AS student_name, " +
            "           s.course_id, c.course_name, s.score AS current_score, " +
            "           LAG(s.id)    OVER w AS previous_id, " +
            "           LAG(s.score) OVER w AS previous_score " +
            "    FROM score s " +
            "    INNER JOIN student st ON s.student_id = st.id " +
            "    INNER JOIN course  c  ON s.course_id  = c.id " +
            "    WINDOW w AS (PARTITION BY s.student_id, c.course_name " +
            "                 ORDER BY s.create_time, s.id) " +
            ") t " +
            "WHERE t.previous_score IS NOT NULL " +
            "  AND (t.previous_score - t.current_score) > #{threshold}")
    List<Map<String, Object>> detectScoreDrops(@Param("threshold") java.math.BigDecimal threshold);

    /**
     * 单个班级在指定课程下的统计指标：人数、平均分、中位数、标准差、及格率。
     * 全部统计在 SQL 层完成。
     * 中位数采用 MySQL 8 窗口函数法（取中间一/二位求平均）。
     */
    Map<String, Object> selectClassStats(@Param("classId") Long classId,
                                         @Param("courseId") Long courseId);
}
