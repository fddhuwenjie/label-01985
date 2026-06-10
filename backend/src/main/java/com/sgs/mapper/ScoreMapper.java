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

    List<Map<String, Object>> selectStudentScoreTrend(@Param("studentId") Long studentId,
                                                      @Param("courseId") Long courseId,
                                                      @Param("courseName") String courseName);

    List<Map<String, Object>> selectClassScoreComparison(@Param("class1Id") Long class1Id,
                                                          @Param("class2Id") Long class2Id,
                                                          @Param("courseId") Long courseId);

    List<Map<String, Object>> selectConsecutiveScoreDrops(@Param("threshold") Double threshold);
}
