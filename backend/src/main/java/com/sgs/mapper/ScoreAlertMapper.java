package com.sgs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgs.entity.ScoreAlert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;
import java.util.Map;

public interface ScoreAlertMapper extends BaseMapper<ScoreAlert> {

    @Select("<script>" +
            "SELECT sa.*, st.name AS studentName, c.course_name AS courseName " +
            "FROM score_alert sa " +
            "LEFT JOIN student st ON sa.student_id = st.id " +
            "LEFT JOIN course c ON sa.course_id = c.id " +
            "WHERE 1=1 " +
            "<if test='status != null'> AND sa.status = #{status} </if>" +
            "<if test='alertLevel != null and alertLevel != &quot;&quot;'> AND sa.alert_level = #{alertLevel} </if>" +
            "<if test='studentId != null'> AND sa.student_id = #{studentId} </if>" +
            "ORDER BY sa.create_time DESC" +
            "</script>")
    Page<Map<String, Object>> selectAlertPage(Page<?> page,
                                               @Param("status") Integer status,
                                               @Param("alertLevel") String alertLevel,
                                               @Param("studentId") Long studentId);

    @Update("UPDATE score_alert SET status = 1, update_time = NOW() WHERE id = #{id}")
    int markAsProcessed(@Param("id") Long id);
}
